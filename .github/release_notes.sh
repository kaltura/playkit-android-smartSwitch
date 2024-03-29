#!/bin/bash
# This script generates "release_notes.md".
# It grabs the logs from the previous defined tag till the HEAD
# and do grep of the PULL requests by filtering using '(#'
# While generating a release notes file, it has 3 sections
# 'New Features', 'Bug Fixes' and 'More Changes'

# STRICT RULES FOR PULL REQUEST SUBJECT LINE:
## 1. 'New Feature' PR should start with 'feat(FEC-***)'. Add '|' Pipe symbol should be added before subject line starts.
##    Example: feat(FEC-1234) | PR Subject line
## 2. 'Bug Fixes' PR should start with 'fix(FEC-***)'. Add '|' Pipe symbol should be added before subject line starts.
##    Example: fix(FEC-1234) | PR Subject line
## 3. 'Other Changes' PR which is apart from the above can start like
##    Example: FEC-1234 | PR Subject line

nl=$'\n'
touch $RELEASE_NOTES
echo "## Changes from [$PREV_TAG](https://github.com/kaltura/$REPO_NAME/releases/tag/$PREV_TAG)$nl" > $RELEASE_NOTES

resultedLine=$(git log $PREV_TAG..HEAD --oneline --grep='(#')
if [[ ! -n "$resultedLine" ]]; then
  echo "### KalturaPlayer Support$nl v$NEW_VERSION" >> $RELEASE_NOTES
else
  git log $PREV_TAG..HEAD --oneline --grep='(#' | cut -d' ' -f2- | while read -r line; do
      echo "$line"

      bugFixes="Bug Fixes"
      newFeatures="New Features"
      moreChanges="More Changes"

      if [[ "$line" == "fix"* || "$line" == "fix(FEC-"* || "$line" == "fix (FEC-"* ]]; then

        grep -qF -- $bugFixes $RELEASE_NOTES || echo "### "$bugFixes$nl >> $RELEASE_NOTES
                modifiedLine=$(echo "$line" | sed 's/fix://' | sed 's/fix//' | sed 's|(\(FEC-[^)]*\))|\1|')
sed -i '/'"$bugFixes"'/a\
'"- $modifiedLine$nl"'' $RELEASE_NOTES

      elif [[ "$line" == "feat"* || "$line" == "feat(FEC-"* || "$line" == "feat (FEC-"* ]]; then

        grep -qF -- $newFeatures $RELEASE_NOTES || echo "### "$newFeatures$nl >> $RELEASE_NOTES
                modifiedLine=$(echo "$line" | sed 's/feat://' | sed 's/feat//' | sed 's|(\(FEC-[^)]*\))|\1|')
sed -i '/'"$newFeatures"'/a\
'"- $modifiedLine$nl"'' $RELEASE_NOTES

      else
        grep -qF -- $moreChanges $RELEASE_NOTES || echo "### "$moreChanges$nl >> $RELEASE_NOTES
        echo "- $line$nl" >> $RELEASE_NOTES

      fi
  done
fi

echo "### Gradle" >> $RELEASE_NOTES
echo "$nl* \`implementation 'com.kaltura.playkit:smartswitchplugin:$NEW_VERSION"\'\` >> $RELEASE_NOTES
echo "$nl [Sample](https://github.com/kaltura/kaltura-player-android-samples/tree/master/AdvancedSamples/SmartSwitch)" >> $RELEASE_NOTES
