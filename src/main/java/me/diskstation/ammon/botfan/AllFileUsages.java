/*
 * Copyright 2007 Tobias Knerr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors:
 * Tobias Knerr
 *
 */
package me.diskstation.ammon.botfan;

import java.util.Iterator;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.queries.BaseQuery;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import net.sourceforge.jwbf.mediawiki.actions.util.RedirectFilter;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action class using the MediaWiki-api's "list=allpages".
 *
 * @author Tobias Knerr
 * @author Thomas Stock
 */
public class AllFileUsages extends BaseQuery<String> {

    private static final Logger log = LoggerFactory.getLogger(AllFileUsages.class);

    /**
     * Constant value for the aplimit-parameter. *
     */
    private static final int LIMIT = 500;

    /**
     * Information given in the constructor, necessary for creating next action.
     */
    private final String prefix;
    private final String from;

    /**
     * The public constructor. It will have an MediaWiki-request generated,
     * which is then added to msgs. When it is answered, the method
     * processAllReturningText will be called (from outside this class).
     *
     * @param from page title to start from, may be null
     * @param prefix restricts search to titles that begin with this value, may
     * be null
     * @param rf include redirects in the list
     * @param namespaces the namespace(s) that will be searched for links, as a
     * string of numbers separated by '|'; if null, this parameter is omitted
     * TODO are multible namespaces allowed?
     */
    public AllFileUsages(MediaWikiBot bot, String from, String prefix) {
        super(bot);
        this.prefix = prefix;
        this.from = from;
    }

    /**
     * Generates the next MediaWiki-request (GetMethod) and adds it to msgs.
     *
     * @param from page title to start from, may be null
     * @param prefix restricts search to titles that begin with this value, may
     * be null
     * @param rf include redirects in the list
     * @param namespace the namespace(s) that will be searched for links, as a
     * string of numbers separated by '|'; if null, this parameter is omitted
     * @return a
     */
    protected Get generateRequest(String from, String prefix) {
        RequestBuilder requestBuilder = new ApiRequestBuilder() //
                .action("query") //
                //.paramNewContinue(bot().getVersion()) //
                .formatXml() //
                .param("list", "allimages") //
                //.param("apfilterredir", findRedirectFilterValue(rf))
                .param("ailimit", LIMIT) //
                ;

        if (from.length() > 0) {
            requestBuilder.param("aifrom", from); //.split("\\|")[1] );//+ MediaWiki.urlEncode("|") + from.split("\\|")[1]);
        }
        return requestBuilder.buildGet();
    }

    /**
     * Picks the article name from a MediaWiki api response.
     *
     * @param s text for parsing
     * @return a
     */
    @Override
    protected ImmutableList<String> parseElements(String s) {
        ImmutableList.Builder<String> titles = ImmutableList.builder();
        Optional<XmlElement> child = XmlConverter.getChildOpt(s, "query", "allimages");
        if (child.isPresent()) {
            for (XmlElement pageElement : child.get().getChildren("img")) {
                //System.out.println(pageElement.getText());
                String title = pageElement.getAttributeValue("title");
                System.out.println(title);
                log.debug("Found image title: \"{}\"", title);
                titles.add(title);
            }
        }
        return titles.build();
    }

    /**
     * Gets the information about a follow-up page from a provided api response.
     * If there is one, a new request is added to msgs by calling
     * generateRequest. If no exists, the string is empty.
     *
     * @param xml text for parsing
     * @return the
     */
    @Override
    protected Optional<String> parseHasMore(final String xml) {
        return parseXmlHasMore(xml, "allimages", "aicontinue", "aicontinue");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpAction prepareNextRequest() {
        Optional<String> urcontinue = nextPageInfoOpt();
        if (urcontinue.isPresent()) {
            return generateRequest(urcontinue.get(), "");
        } else {
            return generateRequest("", "");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<String> copy() {
        return new AllFileUsages(bot(), from, prefix);
    }

}
