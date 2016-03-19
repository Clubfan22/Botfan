/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;


/**
 *
 * @author Marco
 */
   


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
public class UnreviewedPagesTitles extends BaseQuery<String> {

  //private static final Logger log = LoggerFactory.getLogger(net.sourceforge.jwbf.mediawiki.actions.queries.UnreviewedPagesTitles.class);

  /**
   * value for the urlimit-parameter. 
   *
   */
  private static int LIMIT = 50;

  private final MediaWikiBot bot;

  private final int[] namespaces;

  /**
   * generates the next MediaWiki-request (GetMethod) and adds it to msgs.
   *
   * @param namespace the namespace(s) that will be searched for links, as a string of numbers
   *                  separated by '|'; if null, this parameter is omitted
   * @param urstart   Start listing at this page title
   * 
   * @param urend     Stop listing at this page title
   * 
   */
  
  private HttpAction generateRequest(int[] namespace, String urstart, String urend) {
    
    RequestBuilder requestBuilder = new ApiRequestBuilder() //
        .action("query") //
        .formatXml() //
        .param("list", "unreviewedpages") //
        .param("urfilterlevel", 0)
        .param("urlimit", LIMIT);
        
    if (namespace != null) {
      requestBuilder.param("urnamespace", MediaWiki.urlEncode(MWAction.createNsString(namespace)));
    }
    if (urstart.length() > 0) {
      requestBuilder.param("urstart", urstart);
    }
    if(urend.length() > 0){
        requestBuilder.param("urend", urend);
    }

    return requestBuilder.buildGet();

  }
  private HttpAction generateRequest(int[] namespace, String urstart) {
      return generateRequest(namespace, urstart, "");
  }
  
  private HttpAction generateRequest(int[] namespace) {
    return generateRequest(namespace, "", "");
  }

  /**
   *
   */
  public UnreviewedPagesTitles(MediaWikiBot bot, int... ns) {
    super(bot);
    namespaces = ns;
    this.bot = bot;
  }

  /**
   *
   */
  public UnreviewedPagesTitles(MediaWikiBot bot) {
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
        //Not sure about this, RecentChanges doesn't have timestamp either
        //setNextPageInfo(xmlElement.getAttributeValue("title"));
      } else {
        findContent(xmlElement, titleCollection);
      }

    }
  }

  @Override
  protected HttpAction prepareNextRequest() {
    Optional<String> urcontinue = nextPageInfoOpt();
    if (urcontinue.isPresent()) {
        return generateRequest(namespaces, urcontinue.get());
    } else {
      return generateRequest(namespaces);
    }

  }

  @Override
  protected Iterator<String> copy() {
    return new me.diskstation.ammon.botfan.UnreviewedPagesTitles(bot, namespaces);
  }

  
    @Override
  protected Optional<String> parseHasMore(final String xml) {
    return parseXmlHasMore(xml, "unreviewedpages", "urstart", "urstart");
  }

}

