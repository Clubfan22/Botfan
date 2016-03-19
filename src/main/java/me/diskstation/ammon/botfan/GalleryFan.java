/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.actions.queries.CategoryMembersSimple;
import net.sourceforge.jwbf.mediawiki.actions.queries.TemplateUserTitles;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 *
 * @author Marco Ammon <ammon.marco@t-online.de>
 */
public class GalleryFan extends Botfan {

    private HttpActionClient client;

    public GalleryFan() {
        super();
    }

    public GalleryFan(HttpActionClient client) {
        super(client);
        this.client = client;
    }
    
    public static GalleryFan forWiki(String wiki, int requestsPerMinute){
        GalleryFan gf = new GalleryFan(HttpActionClient.builder()
                .withUrl("http://wiki.teamliquid.net/" + wiki + "/") 
                .withUserAgent("Botfan", "0.1", "Clubfan") 
                .withRequestsPerUnit(requestsPerMinute, TimeUnit.MINUTES) 
                .build());
        gf.loginFromPrompt();
        return gf;
    }
    
    
    public void changeGalleryMode(String now, String wanted) {
        TemplateUserTitles cms = new TemplateUserTitles(this, "Template:Infobox player");
        while (cms.hasNext()) {
            String title = cms.next();
            System.out.println(title);
            changeGalleryMode(title, now, wanted);
        }
    }
    
    //Used on every page transcluding "Template:Infobox player"
    protected void changeGalleryMode(String title, String now, String wanted) {
        if (!title.startsWith(".")) {
            Article a = getArticle(title);
            String old = a.getText();
            String updated = old.replace(now, wanted);
            if (!updated.equals(old)) {
                a.setText(updated);
                a.setEditSummary("Converted from " + now + " to " + wanted);
                a.setMinorEdit(true);
                a.save();
            }
        }
    }

    public static void main(String[] args) {

        String[] wikis = {"dota2", "hearthstone", "smash", "heroes", "overwatch"};
        for (String wiki : wikis) {
            GalleryFan gf = GalleryFan.forWiki(wiki, 5);
            System.out.println(wiki);
            System.out.println("__________________________________________");

            TemplateUserTitles cms = new TemplateUserTitles(gf, "Template:Infobox player");
            while (cms.hasNext()) {
                String title = cms.next();
                System.out.println(title);
                if (!title.contains(".")) {
                    gf.changeGalleryMode(title, "mode=\"packed\"", "mode=\"packed-hover\"");
                }
            }
        }
    }
}
