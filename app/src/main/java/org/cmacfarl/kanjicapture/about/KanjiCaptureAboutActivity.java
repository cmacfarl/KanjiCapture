package org.cmacfarl.kanjicapture.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

import org.cmacfarl.kanjicapture.KanjiCaptureApplication;
import org.cmacfarl.kanjicapture.R;

import androidx.annotation.NonNull;

public class KanjiCaptureAboutActivity extends MaterialAboutActivity
{
    @Override
    @NonNull
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {

        MaterialAboutTitleItem titleItem = new MaterialAboutTitleItem.Builder()
                .text(R.string.app_name)
                .icon(R.mipmap.ic_launcher)
                .build();

        MaterialAboutActionItem companyItem = new MaterialAboutActionItem.Builder()
                .text("Author")
                .subText(R.string.cmacfarl)
                .icon(R.drawable.ic_outline_info)
                .build();

        MaterialAboutActionItem versionItem = new MaterialAboutActionItem.Builder()
                .text("Version")
                .subText(KanjiCaptureApplication.getVersionName())
                .icon(R.drawable.ic_outline_info)
                .build();

        MaterialAboutActionItem websiteItem = new MaterialAboutActionItem.Builder()
                .text("Meanings/readings courtesy of the KANJIDIC dictionary file.")
                .subText(R.string.kanjidic_website)
                .icon(R.drawable.ic_website)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(getString(R.string.kanjidic_website));
                        i.setData(uri);
                        if (i.resolveActivity(getPackageManager()) != null) {
                            startActivity(i);
                        }
                    }
                })
                .build();

        MaterialAboutCard title = new MaterialAboutCard.Builder()
                .addItem(titleItem)
                .addItem(companyItem)
                .addItem(versionItem)
                .addItem(websiteItem)
                .build();

        return new MaterialAboutList.Builder()
                .addCard(title)
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_about);
    }
}
