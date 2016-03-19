/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.BaseQuery;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets a list of pages recently changed, ordered by modification timestamp. Parameters: rcfrom
 * (paging timestamp), rcto (flt), rcnamespace (flt), rcminor (flt), rcusertype (dflt=not|bot),
 * rcdirection (dflt=older), rclimit (dflt=10, max=500/5000) F api.php ? action=query &amp;
 * list=recentchanges - List last 10 changes
 *
 * @author Thomas Stock
 */
public class PendingChangesTitles extends BaseQuery<String> {

  //private static final Logger log = LoggerFactory.getLogger(net.sourceforge.jwbf.mediawiki.actions.queries.RecentchangeTitles.class);

  /**
   * value for the bllimit-parameter. *
   */
  private static final int LIMIT = 50;

  private final MediaWikiBot bot;

  private final int[] namespaces;

  /**
   * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
   *
   * @param namespace the namespace(s) that will be searched for links, as a string of numbers
   *                  separated by '|'; if null, this parameter is omitted
   * @param orstart   timestamp
   */
  private HttpAction generateRequest(int[] namespace, String orstart) {

    RequestBuilder requestBuilder = new ApiRequestBuilder() //
        .action("query") //
        .formatXml() //
        .param("list", "oldreviewedpages") //
        .param("orlimit", LIMIT) //
        ;
    if (namespace != null) {
      requestBuilder.param("ornamespace", MediaWiki.urlEncode(MWAction.createNsString(namespace)));
    }
    if (orstart.length() > 0) {
      requestBuilder.param("orstart", orstart);
    }

    return requestBuilder.buildGet();

  }

  private HttpAction generateRequest(int[] namespace) {
    return generateRequest(namespace, "");
  }

  /**
   *
   */
  public PendingChangesTitles(MediaWikiBot bot, int... ns) {
    super(bot);
    namespaces = ns;
    this.bot = bot;

  }

  /**
   *
   */
  public PendingChangesTitles(MediaWikiBot bot) {
    this(bot, MediaWiki.NS_ALL);
  }

  /**
   * picks the article name from a MediaWiki api response.
   *
   * @param s text for parsing
   */
  @Override
  protected ImmutableList<String> parseElements(String s) {
    XmlElement root = XmlConverter.getRootElement(s);
    List<String> titleCollection = Lists.newArrayList();
    findContent(root, titleCollection);
    return ImmutableList.copyOf(titleCollection);

  }

  private void findContent(final XmlElement root, List<String> titleCollection) {

    for (XmlElement xmlElement : root.getChildren()) {
      if (xmlElement.getQualifiedName().equalsIgnoreCase("p")) {
        titleCollection.add(MediaWiki.htmlUnescape(xmlElement.getAttributeValue("title")));
        
      } else if (xmlElement.getQualifiedName().equalsIgnoreCase("oldreviewedpages")) {
          setNextPageInfo(xmlElement.getAttributeValue("orstart"));
      } else {
        findContent(xmlElement, titleCollection);
      }

    }
  }

  @Override
  protected HttpAction prepareNextRequest() {
    if (hasNextPageInfo()) {
      return generateRequest(namespaces, getNextPageInfo());
    } else {
      return generateRequest(namespaces);
    }

  }

  @Override
  protected Iterator<String> copy() {
    return new me.diskstation.ammon.botfan.PendingChangesTitles(bot, namespaces);
  }

  @Override
  protected Optional<String> parseHasMore(String s) {
    return Optional.absent();
  }

}
