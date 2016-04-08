package me.diskstation.ammon.botfan;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import static me.diskstation.ammon.botfan.PurgePage.MODE_FORCE_LINKUPDATE;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
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
        return bf;
    }

    public void purgePage(String articlename) {
        System.out.println(articlename);
		articlename = articlename.replace(".", "%2E");
        PurgePage pp = new PurgePage(getUserinfo(), MediaWiki.urlEncode(articlename));
        client.performAction(pp);
    }

    protected void purgePages(String[] titles) {
        PurgePage pp = new PurgePage(getUserinfo(), titles);
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

    public static void main(String[] args) {
        Botfan bf = Botfan.forWiki("dota2", 30);
        bf.purgeUsages("Template:Upcoming and ongoing matches of");
    }
}
