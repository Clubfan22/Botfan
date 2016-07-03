package me.diskstation.ammon.botfan;

import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.TemplateUserTitles;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * Hello world!
 *
 */
public class Botfan extends MediaWikiBot {

    private HttpActionClient client;

    public Botfan() {
        super();
    }

    public Botfan(HttpActionClient client) {
        super(client);
        this.client = client;
    }

    public static Botfan forWiki(String wiki, int requestsPerMinute) {
        Botfan bf = new Botfan(HttpActionClient.builder()
                .withUrl("http://wiki.teamliquid.net/" + wiki + "/")
                .withUserAgent("Botfan", "0.1", "Clubfan")
                .withRequestsPerUnit(requestsPerMinute, TimeUnit.MINUTES)
                .build());
        bf.loginFromPrompt();
		//bf.login("username", "password");
        return bf;
    }

    public void purgePage(String articlename) {
        System.out.println(articlename);
		articlename = articlename.replace(".", "%2E");
        PurgePage pp = new PurgePage(getUserinfo(), MediaWiki.urlEncode(articlename), PurgePage.MODE_PURGE);
        client.performAction(pp);
    }

    protected void purgePages(String[] titles) {
        PurgePage pp = new PurgePage(getUserinfo(), titles, PurgePage.MODE_PURGE);
        client.performAction(pp);
    }

    protected void purgeUsages(String template) {
        Iterator<String> titles = new TemplateUserTitles(this, template, MediaWiki.NS_MAIN);
        while (titles.hasNext()) {
            purgePage(titles.next());
        }
    }

    public void loginFromPrompt() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Login as:");
        String username = sc.nextLine();
        System.out.println("Password:");
        String password = sc.nextLine();
        login(username, password);
    }
	
	public void replace(String page, String old, String n){
		System.out.println(page);
		Article a = getArticle(page);
		String text = a.getText();
		text = text.replaceAll(old, n);
		a.setText(text);
		a.setMinorEdit(true);
		a.setEditSummary("Removed unnecessary parameter |number=");
		a.save();
		//System.out.println(text);
	}
	public void replaceOnAllUsages(String template, String old, String n){
		TemplateUserTitles tut = new TemplateUserTitles(this, template, MediaWiki.NS_ALL);
		boolean skip = false;
		while (tut.hasNext()){
			String page = tut.next();
			if (page.equals("Go4Heroes/Europe/Weekly/28")){
				skip = false;
			}
			if (!skip){
				replace(page, old, n);
			}
			
		}
	}
    public static void main(String[] args) {
		String[] wikis = {"dota2"}; //counterstrike
		for (String wiki : wikis){
			Botfan bf = Botfan.forWiki(wiki, 30);
			bf.purgeUsages("Template:Infobox player");
			//bf.replaceOnAllUsages("Template:Infobox league", "\\|number\\=[0-9]*\\n", "");
		}        
    }
}
