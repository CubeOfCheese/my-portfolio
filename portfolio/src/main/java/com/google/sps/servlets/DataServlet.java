// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private ArrayList<String> referrals;
  private DatastoreService datastore;

  @Override
  public void init() {
    referrals = new ArrayList<String>();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text");
    Query query = new Query("Referral").addSort("sentiment", SortDirection.ASCENDING);;
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
        String author = (String) entity.getProperty("author");
        String referralContent = (String) entity.getProperty("referralContent");
        double sentiment = (double) entity.getProperty("sentiment");
        if (sentiment > 0) {
            referrals.add(String.format("{\"author\": \"%s\", \"content\": \"%s\"}", author, referralContent));
        }
    }
    response.getWriter().println(referrals);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String author = request.getParameter("author");
    String referralContent = request.getParameter("referralContent");
    Entity referralEntity = new Entity("Referral");
    referralEntity.setProperty("author", author);
    referralEntity.setProperty("referralContent", referralContent);
    Document doc = Document.newBuilder().setContent(referralContent).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();
    referralEntity.setProperty("sentiment", score);
    datastore.put(referralEntity);
    response.sendRedirect("/");
  }
}
