package me.diskstation.ammon.botfan;

import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import net.sourceforge.jwbf.core.actions.HttpActionClient;
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

    public void purgePage(final String articlename) {
        System.out.println(articlename);
        PurgePage pp = new PurgePage(getUserinfo(), articlename);
        client.performAction(pp);
    }

    protected void purgePages(String[] titles) {
        PurgePage pp = new PurgePage(getUserinfo(), titles);
        client.performAction(pp);
    }

    protected void purgeUsages(String template) {
        Iterator<String> titles = new TemplateUserTitles(this, template);
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
        Botfan bf = Botfan.forWiki("dota2", 10);
        bf.purgeUsages("Template:Upcoming and ongoing matches of");
    }
}
