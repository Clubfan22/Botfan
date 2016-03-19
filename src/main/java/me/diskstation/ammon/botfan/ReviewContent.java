/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import net.sourceforge.jwbf.core.actions.Post;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.core.contentRep.ContentAccessable;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.core.contentRep.Userinfo;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.editing.GetApiToken;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.actions.util.VersionException;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes an article.
 *
 * @author Thomas Stock
 */
public class ReviewContent extends MWAction {

  //private static final Logger log = LoggerFactory.getLogger(net.sourceforge.jwbf.mediawiki.actions.editing.PostModifyContent.class);

  private boolean first = true;
  private boolean second = true;

  private final ContentAccessable a;
  private final String revisionId;
  private final MediaWikiBot bot;
  private GetApiToken editTokeAction = null;
  private HttpAction apiGet = null;
  private Post editRequest = null;
  static final String PARAM_MINOR = "minor";
  static final String PARAM_MINOR_NOT = "notminor";
  static final String PARAM_BOTEDIT = "bot";

  public ReviewContent(MediaWikiBot bot, final SimpleArticle a) {
    if (Strings.isNullOrEmpty(a.getTitle())) {
      throw new ActionException("imposible request, no title");
    }
    this.a = a;
    this.bot = bot;
    revisionId = a.getRevisionId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HttpAction getNextMessage() {
      
    Userinfo userinfo = bot.getUserinfo();
    Set<String> rights = userinfo.getRights();
    boolean canWrite = rights.contains(Userinfo.RIGHT_EDIT) && //
        rights.contains(Userinfo.RIGHT_WRITEAPI);

    if (!canWrite) {
      throw new VersionException("editing is not allowed");
    }
    if (first) {
      first = false;
      editTokeAction = newTokenRequest();
      apiGet = editTokeAction.popAction();
      return apiGet;
    } else if (second) {

      RequestBuilder builder = new ApiRequestBuilder() //
          .action("review") //
          .formatXml() //
          //.param("title", MediaWiki.urlEncode(a.getTitle())) //
          .postParam("revid", revisionId)
          //.postParam("flag_accuracy", 1) 
          //.postParam("flag_depth", 1) 
          //.postParam("flag_style", 1) 
          //.postParam("text", a.getText()) //
          ;
      Set<String> groups = userinfo.getGroups();
      
      // postModify.addParam("watch", "unknown")
      //if (a.isMinorEdit()) {
        //builder.postParam(PARAM_MINOR, "");
      //} else {
        //builder.postParam(PARAM_MINOR_NOT, "");
      //}
      builder.postParam(editTokeAction.get().token());
      second = false;

      editRequest = builder.buildPost();
      return editRequest;
    } else {
      throw new IllegalStateException("this action has only two messages");
    }
  }

  /**
   * TODO only for testing
   */
  GetApiToken newTokenRequest() {
    return new GetApiToken(GetApiToken.Intoken.EDIT, a.getTitle());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasMoreMessages() {
    return first || second;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String processReturningText(String xml, HttpAction hm) {
    String request = hm.getRequest();
    if (request.equals(apiGet.getRequest())) {
      editTokeAction.processReturningText(xml, hm);
    } else if (request.equals(editRequest.getRequest())) {
      // FIXME feels very strage
      XmlConverter.getRootElement(xml);
    } else {
      //log.trace(xml);
      throw new ActionException("unknown response");
    }

    return xml;
  }

  /**
   * @return true if one or both sets are <code>null</code> or the intersection of sets is empty.
   */
  boolean isIntersectionEmpty(Set<?> a, Set<?> b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    Sets.SetView<?> intersection = Sets.intersection(a, b);
    return intersection.isEmpty();
  }

}
