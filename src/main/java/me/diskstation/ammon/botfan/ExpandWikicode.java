package me.diskstation.ammon.botfan;

import java.util.NoSuchElementException;

import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements function to render wikitext on remote.
 *
 * @author Thomas Stock
 * @see
 * <a href="http://www.mediawiki.org/wiki/API:Expanding_templates_and_rendering#parse">
 * API:Parsing wikitext</a>
 */
public class ExpandWikicode extends MWAction {

    private static final Logger log = LoggerFactory.getLogger(ExpandWikicode.class);

    private final Get msg;
    private String html = "";
    private final MediaWikiBot bot;
    private boolean isSelfEx = true;

    public ExpandWikicode(MediaWikiBot bot, String wikitext) {
        this.bot = bot;
        msg = new ApiRequestBuilder() //
                .action("expandtemplates") //
                .formatXml() //
                .param("text", MediaWiki.urlEncode(wikitext)) //
                .param("title", "API") //
                .buildGet();

    }

    /**
     * {@inheritDoc}
     *
     * @deprecated see super
     */
    @Deprecated
    @Override
    public boolean isSelfExecuter() {
        return isSelfEx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpAction getNextMessage() {
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String processAllReturningText(String s) {
        html = findElement("expandtemplates", s).getText();
        //html = html.replace("\n", "");
        return "";
    }

    protected XmlElement findElement(String elementName, String xml) {
        XmlElement root = XmlConverter.getRootElement(xml);
        return findContent(root, elementName);
    }

    private XmlElement findContent(final XmlElement e, final String name) {
        XmlElement found = null;
        for (XmlElement xmlElement : e.getChildren()) {
            if (xmlElement.getQualifiedName().equalsIgnoreCase(name)) {
                return xmlElement;

            } else {
                found = findContent(xmlElement, name);
            }

        }
        if (found == null) {
            throw new NoSuchElementException();
        }
        return found;
    }

    private void update() {
        try {
            isSelfEx = false;
            bot.getPerformedAction(this);

        } catch (ActionException | ProcessException e) {
            log.warn("", e);
        } finally {
            isSelfEx = true;
        }
    }

    /**
     * @return the
     */
    public String getHtml() {
        if (html.length() < 1) {
            update();
        }
        return html;
    }

}
