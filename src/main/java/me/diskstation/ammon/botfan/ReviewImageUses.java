/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles;
import net.sourceforge.jwbf.mediawiki.actions.queries.ImageUsageTitles;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 *
 * @author Marco
 */
public class ReviewImageUses extends Botfan {

    public ReviewImageUses(HttpActionClient client) {
        super(client);
    }

    public static ReviewImageUses forWiki(String wiki, int requestsPerMinute) {
        ReviewImageUses riu = new ReviewImageUses(HttpActionClient.builder()
                .withUrl("http://wiki.teamliquid.net/" + wiki + "/")
                .withUserAgent("Botfan", "0.1", "Clubfan")
                .withRequestsPerUnit(requestsPerMinute, TimeUnit.MINUTES)
                .build());
        riu.loginFromPrompt();
        return riu;
    }

    public Iterator<String> getAllFileUsingPages(Iterator<String> files) {
        HashSet<String> fiup = new HashSet();
        while (files.hasNext()) {
            String title = files.next();
            ImageUsageTitles iut = new ImageUsageTitles(this, title);
            while (iut.hasNext()) {
                fiup.add(iut.next());
            }
        }
        return fiup.iterator();
    }

    public Iterator<String> getAllFiles() {
        return new AllFileUsages(this, "", "");
    }

    public Iterator<String> getAllPages() {
        return new AllPageTitles(this, MediaWiki.NS_MAIN);
    }

    // Reviews all pages which use the specified image and are reviewed
    public void reviewAllPagesUsingImage(String image) {
        Iterator<FlaggedInformation> titles = new ImageUsageFlaggedTitles(this, image).iterator();
        while (titles.hasNext()) {
            FlaggedInformation fi = titles.next();
            String title = fi.getTitle();
            System.out.println(title);
            Article a = getArticle(title);
            if (fi.isStable()) {
                reviewPage(a);
            }
        }
    }

    public boolean isReviewed(Article a) {
        GetFlaggedInformation gfi = new GetFlaggedInformation(a.getTitle());
        getPerformedAction(gfi);
        System.out.println("Checking review state of " + a.getTitle());
        FlaggedInformation fi = getPerformedAction(new GetFlaggedInformation(a.getTitle())).getFlaggedInformation();
        String currentRevId = a.getRevisionId();
        String stableRevId = fi.getStableRevId();
        if (currentRevId.equals(stableRevId)) {
            return true;
        } else {
            return false;
        }
    }

    void reviewPage(Article a) {
        System.out.println("Reviewing " + a.getTitle());
        getPerformedAction(new ReviewContent(this, a.getSimpleArticle()));
    }

    void reviewPage(String title) {
        Article a = getArticle(title);
        reviewPage(a);
    }

    public static void main(String[] args) {

        /* 
        *  This script reviews all pages on all wikis which are already reviewed.
        *  While this might sound contradictory, it was needed when we moved 
        *  all images to LP-Commons.
         */
        String[] wikis = {"smash", "starcraft2", "overwatch", "hearthstone",
            "dota2", "heroes", "smash", "counterstrike"};
        for (String wiki : wikis) {
            ReviewImageUses riu = ReviewImageUses.forWiki(wiki, 10);
            Iterator<String> pages = riu.getAllPages();
            while (pages.hasNext()) {
                String page = pages.next();
                System.out.println(page);
                Article a = riu.getArticle(page);
                if (riu.isReviewed(a)) {
                    riu.reviewPage(a);
                }
            }
        }
    }
}
