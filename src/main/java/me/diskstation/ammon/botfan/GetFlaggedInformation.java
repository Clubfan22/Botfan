/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;


/*
 * Copyright 2007 Thomas Stock.
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
 *
 */
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.HashSet;
import net.sourceforge.jwbf.core.actions.Get;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.core.contentRep.SimpleArticle;
import net.sourceforge.jwbf.mapper.XmlConverter;
import net.sourceforge.jwbf.mapper.XmlElement;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.MediaWiki.Version;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the content of a given article.
 *
 * @author Thomas Stock
 */
public class GetFlaggedInformation extends MWAction {

    private static final Logger log = LoggerFactory.getLogger(net.sourceforge.jwbf.mediawiki.actions.editing.GetRevision.class);
    private List<FlaggedInformation> flaggedInformations = Lists.newArrayList();
    private final ImmutableList<String> names;
    private final Get msg;

    /**
     * TODO follow redirects. TODO change constructor field ordering; bot
     */
    public GetFlaggedInformation(MediaWiki.Version v, String articlename) {
        this(ImmutableList.of(articlename));
    }

    public GetFlaggedInformation(String articlename) {
        this(ImmutableList.of(articlename));
    }

    public GetFlaggedInformation(ImmutableList<String> names) {
        this.names = names;
        // TODO continue=-||
        msg = new ApiRequestBuilder() //
                .action("query") //
                .formatXml() //
                .param("prop", "flagged") //
                .param("titles", MediaWiki.urlEncode(MediaWiki.pipeJoined(names))) //
                .buildGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String processReturningText(final String s, HttpAction ha) {
        if (msg.getRequest().equals(ha.getRequest())) {
            parse(s);
        }
        return "";
    }

    private void parse(final String xml) {

        Optional<XmlElement> childOpt = XmlConverter.getChildOpt(xml, "query", "pages");
        if (childOpt.isPresent()) {
            List<XmlElement> pages = childOpt.get().getChildren("page");
            for (XmlElement page : pages) {

                String title = page.getAttributeValue("title");
                XmlElement flag = page.getChild("flagged");
                FlaggedInformation fi = new FlaggedInformation(title);
                fi.setStableRevId(flag.getAttributeValue("stable_revid"));
                String level = flag.getAttributeValue("level");
                if (level != null && level.length() > 0){
                    fi.setLevel(level);
                } else {
                    fi.setLevel("");
                }
                fi.setLevelText(flag.getAttributeValue("level_text"));
                fi.setPendingSince(flag.getAttributeValue("pending_since"));
                String protectionLevel = flag.getAttributeValue("protection_level");
                if (protectionLevel != null && protectionLevel.length() > 0){
                    fi.setProtectionLevel(protectionLevel);
                } else {
                    fi.setProtectionLevel("");
                }
                flaggedInformations.add(fi);

            }
        }

    }

    public FlaggedInformation getFlaggedInformation() {
        return Iterables.getOnlyElement(asList());
    }

    public ImmutableList<FlaggedInformation> asList() {
        return ImmutableList.copyOf(flaggedInformations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpAction getNextMessage() {
        return msg;
    }
}
