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
import com.google.common.collect.ImmutableList;
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
 * action class using the MediaWiki-api's "list=imagelinks" and later
 * imageUsage.
 *
 * @author Tobias Knerr
 * @author Thomas Stock
 * @since MediaWiki 1.9.0
 */
public class ImageUsageFlaggedTitles extends BaseQuery<FlaggedInformation> {

    private static final Logger log = LoggerFactory.getLogger(ImageUsageFlaggedTitles.class);

    private final int limit;

    private final MediaWikiBot bot;

    private final String imageName;
    private final ImmutableList<Integer> namespaces;

    public ImageUsageFlaggedTitles(MediaWikiBot bot, String imageName, int... namespaces) {
        this(bot, 50, imageName, MWAction.nullSafeCopyOf(namespaces));
    }

    ImageUsageFlaggedTitles(MediaWikiBot bot, int limit, String imageName,
            ImmutableList<Integer> namespaces) {
        super(bot);
        this.bot = bot;
        this.limit = limit;
        this.imageName = imageName;
        this.namespaces = namespaces;
    }

    public ImageUsageFlaggedTitles(MediaWikiBot bot, String nextPageInfo) {
        this(bot, nextPageInfo, MediaWiki.NS_ALL);
    }

    /**
     * gets the information about a follow-up page from a provided api response.
     * If there is one, a new request is added to msgs by calling
     * generateRequest.
     *
     * @param xml text for parsing
     */
    @Override
    protected Optional<String> parseHasMore(final String xml) {
        return parseXmlHasMore(xml, "imageusage", "giucontinue", "giucontinue");
    }

    /**
     * picks the article name from a MediaWiki api response.
     *
     * @param s text for parsing
     */
    @Override
    protected ImmutableList<FlaggedInformation> parseElements(String s) {
        ImmutableList.Builder<FlaggedInformation> titleCollection = ImmutableList.builder();
        Optional<XmlElement> childOpt = XmlConverter.getChildOpt(s, "query", "pages");
        if (childOpt.isPresent()) {
            for (XmlElement element : childOpt.get().getChildren("page")) {

                FlaggedInformation fi = new FlaggedInformation(element.getAttributeValue("title"));
                fi.setLastRevId(element.getAttributeValue("lastrevid"));
                XmlElement flag = element.getChild("flagged");
                fi.setStableRevId(flag.getAttributeValue("stable_revid"));
                String level = flag.getAttributeValue("level");
                if (level != null) {
                    fi.setLevel(level);
                } else {
                    fi.setLevel("0");
                }
                fi.setLevelText(flag.getAttributeValue("level_text"));
                fi.setPendingSince(flag.getAttributeValue("pending_since"));
                String protectionLevel = flag.getAttributeValue("protection_level");
                if (protectionLevel != null) {
                    fi.setProtectionLevel(protectionLevel);
                } else {
                    fi.setProtectionLevel("0");
                }
                titleCollection.add(fi);
                //}

            }
        }
        return titleCollection.build();
    }

    @Override
    protected HttpAction prepareNextRequest() {
        RequestBuilder requestBuilder = new ApiRequestBuilder() //
                .action("query") //
                .formatXml() //
                .param("giutitle", MediaWiki.urlEncode(imageName)) //
                .param("generator", "imageusage") //
                .param("giulimit", limit) //
                .param("giunamespace", MediaWiki.urlEncodedNamespace(namespaces));

        Optional<String> ilcontinue = nextPageInfoOpt();
        if (ilcontinue.isPresent()) {
            requestBuilder.param("giucontinue", MediaWiki.urlEncode(ilcontinue.get()));
        }
        return requestBuilder.buildGet();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterator<FlaggedInformation> copy() {
        return new ImageUsageFlaggedTitles(bot, limit, imageName, namespaces);
    }

}
