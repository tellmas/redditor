package com.tellmas.android.redditor;

import java.util.List;

import com.cd.reddit.json.mapping.RedditLink;

/**
 *
 */
public class RedditResponse {

   private final List<RedditLink> links;
   private final String before;
   private final String after;

   public RedditResponse(final List<RedditLink> links, final String before, final String after) {
       this.links = links;
       this.before = before;
       this.after = after;
   }
   public List<RedditLink> getLinksList() {
       return this.links;
   }
   public String getBefore() {
       return this.before;
   }
   public String getAfter() {
       return this.after;
   }

}