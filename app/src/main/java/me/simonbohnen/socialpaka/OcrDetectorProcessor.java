/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.simonbohnen.socialpaka;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */

class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private final Activity context;
    private final String dialogTitle;
    static boolean hasDetected;

    OcrDetectorProcessor(Activity ncontext, String ndialogTitle) {
        context = ncontext;
        dialogTitle = ndialogTitle;
    }

    @Override
    public void release() {

    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                final String wort = item.getValue();
                if(!hasDetected && wort.length() > 1 && Character.isUpperCase(wort.charAt(0)) && wort.matches("[A-Za-z-\\s]+") && Character.isAlphabetic(wort.charAt(wort.length() - 1)) && Character.isAlphabetic(wort.charAt(0))) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OCR", "Name detected: " + wort);
                            ArrayList<String> matches = new ArrayList<>();
                            for(String s : MainActivity.names) {
                                if(s.contains(wort)) {
                                    //todo muss mehrere Treffer verarbeiten können
                                    matches.add(s);
                                }
                            }
                            if(matches.size() > 0) {
                                if(matches.size() == 1) {
                                    String userid = MainActivity.nameToUserID.get(matches.get(0));
                                    AccountDetailActivity.userID = userid;
                                    AccountDetailActivity.user = MainActivity.userInfo.get(userid);
                                    context.startActivity(new Intent(context, AccountDetailActivity.class));
                                } else {
                                    hasDetected = true;
                                    SelectUserDialogFragment dialog = new SelectUserDialogFragment();
                                    dialog.supplyArguments(matches, context, dialogTitle);
                                    dialog.show(context.getFragmentManager(), "SelectUserDialogFragment");
                                }
                            }
                        }
                    }).start();
                } else {
                    Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                }
            }
        }
    }
}
