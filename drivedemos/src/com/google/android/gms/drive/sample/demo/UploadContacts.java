/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.drive.sample.demo;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Environment;
import android.net.Uri;
import android.content.res.AssetFileDescriptor;

/**
 * An activity to illustrate how to create a file.
 */
public class UploadContacts extends BaseDemoActivity {

    private static final String TAG = "CreateFileActivity";
	Cursor cursor;
	ArrayList<String> vCard ;
	String vfile;
	
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // create new contents resource
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }
            final DriveContents driveContents = result.getDriveContents();

            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {
                try {
                	 getVcardString();
                } catch (IOException e) {
                	        // TODO Auto-generated catch block
                	 e.printStackTrace();
                }
                  // write content to DriveContents
                 //   OutputStream outputStream = driveContents.getOutputStream();
                  //  Writer writer = new OutputStreamWriter(outputStream);
                  //  try {
                  //      writer.write("Hello World!");
                  //      writer.close();
                  //  } catch (IOException e) {
                  //      Log.e(TAG, e.getMessage());
                  //  }
                File dir = Environment.getExternalStorageDirectory();
                File file = new File(dir, "ContactsBackup.vcf");
                try {
					FileInputStream inputStream = new FileInputStream(file);
					OutputStream outputStream = driveContents.getOutputStream();
					byte[] buffer = new byte[1024];
	                int len;
	                try {
						while ((len = inputStream.read(buffer)) != -1) {
						    outputStream.write(buffer, 0, len);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("ContactsBackup.vcf")
                            .setMimeType("text/plain")
                            .setStarred(true).build();

                    // create a file on root folder
                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }
            showMessage("Done performing backup: Check you Google Drive account" + result.getDriveFile().getDriveId());
        }
    };
    
    private void getVcardString() throws IOException {

        final String vfile = "ContactsBackup.vcf";
       // TODO Auto-generated method stub
       vCard = new ArrayList<String>();
       cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
       if(cursor!=null&&cursor.getCount()>0)
       {
           int i;
           String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
           FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
           cursor.moveToFirst();
           for(i = 0;i<cursor.getCount();i++)
           {
               get(cursor);
               Log.d("TAG", "Contact "+(i+1)+"VcF String is"+vCard.get(i));
               cursor.moveToNext();
               mFileOutputStream.write(vCard.get(i).toString().getBytes());
           }
           mFileOutputStream.close();
           cursor.close();
       }
       else
       {
           Log.d("TAG", "No Contacts in Your Phone");
       }
   }
   private void get(Cursor cursor2) {
       String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
       Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
       AssetFileDescriptor fd;
       try {
           fd = this.getContentResolver().openAssetFileDescriptor(uri, "r");

           FileInputStream fis = fd.createInputStream();
           byte[] buf = new byte[(int) fd.getDeclaredLength()];
           fis.read(buf);
           String vcardstring= new String(buf);
           vCard.add(vcardstring);
       } catch (Exception e1) 
       {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       }
   }   


}