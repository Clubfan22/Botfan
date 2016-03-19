/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;

/**
 *
 * @author Marco Ammon <ammon.marco@t-online.de>
 */
public class StaticFan extends Botfan {

    private HttpActionClient client;

    public StaticFan() {
        super();
    }

    public StaticFan(HttpActionClient client) {
        super(client);
        this.client = client;
    }
    
    public static StaticFan forWiki(String wiki, int requestsPerMinute){
        StaticFan sf = new StaticFan(HttpActionClient.builder()
                .withUrl("http://wiki.teamliquid.net/" + wiki + "/") 
                .withUserAgent("Botfan", "0.1", "Clubfan") 
                .withRequestsPerUnit(requestsPerMinute, TimeUnit.MINUTES) 
                .build());
        sf.loginFromPrompt();
        return sf;
    } 
    
    public void convertPageToStatic(String inputTitle, String outputTitle) {
        purgePage(inputTitle);
        String wikicode = getRenderedWikicodeFromPage(inputTitle);
        saveWikicodeToPage(outputTitle, wikicode, false);
    }

    protected String getRenderedWikicodeFromPage(String article) {
        //Prepend ":" as namespace identifier for main space
        if (!article.contains(":")) {
            article = ":" + article;
        }
        String wikicode = "{{" + article + "}}";
        ExpandWikicode ew = new ExpandWikicode(this, wikicode);
        return ew.getHtml();
    }

    protected void saveWikicodeToPage(String title, String content, boolean forceUpdate) {
        Article a = getArticle(title);
        if (forceUpdate || !a.getText().equals(content)) {
            a.setText(content);
            a.setMinorEdit(true);
            a.save("Automatic conversion from dynamic page to static wikicode");
        }
    }

    public static void main(String[] args) {
        String[] wikis = {"dota2", "counterstrike", "heroes"};
        for (String wiki : wikis) {
            StaticFan sf = StaticFan.forWiki(wiki, 5);
            String[] inputPages = {"Liquipedia:Upcoming and ongoing matches/dynamic", "Liquipedia:Upcoming and ongoing matches on mainpage/dynamic"};
            String[] outputPages = {"Liquipedia:Upcoming and ongoing matches", "Liquipedia:Upcoming and ongoing matches on mainpage"};
            for (int j = 0; j < inputPages.length; j++) {
                sf.convertPageToStatic(inputPages[j], outputPages[j]);
            }
        }
    }
}
