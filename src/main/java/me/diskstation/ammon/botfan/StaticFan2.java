/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;

/**
 *
 * @author Marco Ammon <ammon.marco@t-online.de>
 */
public class StaticFan2 extends Botfan {

    private HttpActionClient client;

    public StaticFan2() {
        super();
    }

    public StaticFan2(HttpActionClient client) {
        super(client);
        this.client = client;
    }
    
    public static StaticFan2 forWiki(String wiki, int requestsPerMinute){
        StaticFan2 sf = new StaticFan2(HttpActionClient.builder()
                .withUrl("http://wiki.teamliquid.net/" + wiki + "/") 
                .withUserAgent("Botfan", "0.1", "Clubfan") 
                .withRequestsPerUnit(requestsPerMinute, TimeUnit.MINUTES) 
                .build());
        sf.loginFromPrompt();
        //sf.login("username", "password");
        return sf;
    } 
    
    public void convertPageToStatic(String inputTitle, String outputTitle) {
        purgePage(inputTitle);
        String wikicode = getRenderedWikicodeFromPage(inputTitle);
        wikicode = wikicode.replaceAll("\\[\\[SMW::off\\]\\]", "").replaceAll("\\[\\[SMW::on\\]\\]", "");
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
        //if (forceUpdate || !a.getText().equals(content)) {
        if (content.equals("\n")){
            content = "&nbsp;\n";
        }
        a.setText(content);
        a.setMinorEdit(true);
        a.save("Automatic conversion from dynamic page to static wikicode");
        
    }

    public static void main(String[] args) {
        String[] wikis = {"warcraft"};
        for (String wiki : wikis) {
            System.out.println(wiki);
            System.out.println(new Date(System.currentTimeMillis()));
            StaticFan2 sf = StaticFan2.forWiki(wiki, 10);
            String[] inputPages = {"Liquipedia:Tournaments/dynamic"};
            String[] outputPages = {"Liquipedia:Tournaments"};
            for (int j = 0; j < inputPages.length; j++) {
                sf.convertPageToStatic(inputPages[j], outputPages[j]);
            }
        }
    }
}
