/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diskstation.ammon.botfan;

/**
 *
 * @author Marco
 */
public class FlaggedInformation {
    String title;
    String stableRevId;
    String level;
    String levelText;
    String pendingSince;
    String protectionLevel;
    String protectionExpiry;
    String lastRevId;
    boolean isStable;
    
    public FlaggedInformation(String title){
        this.title = title;
    }
    
    public FlaggedInformation(String title, String stableRevId){
        this.title = title;
        this.stableRevId = stableRevId;
    }
    
    private void setStable(String stableRevId, String lastRevId){
        if (stableRevId != null && lastRevId != null){
            isStable = stableRevId.equals(lastRevId);
        } else {
            isStable = false;
        }
    }
    
    public boolean isStable(){
        return isStable;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStableRevId() {
        return stableRevId;
    }

    public void setStableRevId(String stableRevId) {
        this.stableRevId = stableRevId;
        setStable(stableRevId, lastRevId);
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevelText() {
        return levelText;
    }

    public void setLevelText(String levelText) {
        this.levelText = levelText;
    }

    public String getPendingSince() {
        return pendingSince;
    }

    public void setPendingSince(String pendingSince) {
        this.pendingSince = pendingSince;
    }

    public String getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(String protectionLevel) {
        this.protectionLevel = protectionLevel;
    }

    public String getProtectionExpiry() {
        return protectionExpiry;
    }

    public void setProtectionExpiry(String protectionExpiry) {
        this.protectionExpiry = protectionExpiry;
    }

    public String getLastRevId() {
        return lastRevId;
    }

    public void setLastRevId(String lastRevId) {
        this.lastRevId = lastRevId;
        setStable(stableRevId, lastRevId);
    }
    
    
}
