/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles;
import net.sourceforge.jwbf.mediawiki.actions.queries.ImageUsageTitles;

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
		//riu.login("username", "password");
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
        return new AllPageTitles(this, MediaWiki.NS_TEMPLATE);
    }
	
	public Iterator<String> getAllUnreviewedPages() {
		return new UnreviewedPagesTitles(this, MediaWiki.NS_TEMPLATE);
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
		boolean skip = false;
        String[] wikis = {"warcraft"}; //"smash", "starcraft2", "starcraft", "hearthstone", "dota2", "heroes", "counterstrike", "overwatch", 
        for (String wiki : wikis) {
            ReviewImageUses riu = ReviewImageUses.forWiki(wiki, 120);
            Iterator<String> pages = riu.getAllUnreviewedPages();
            while (pages.hasNext()) {
                String page = pages.next();
				if (page.equals("Template:TeamPage/evolve")){
					skip = false;
				}
                System.out.println(page);
				if (!skip){
						Article a = riu.getArticle(page);
					//if (riu.isReviewed(a)) {
						riu.reviewPage(a);
					//}
				}
                
            }
        }
    }
}
