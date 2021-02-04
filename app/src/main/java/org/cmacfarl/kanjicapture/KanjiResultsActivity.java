/*
 * Copyright 2021 Craig MacFarlane.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cmacfarl.kanjicapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.cmacfarl.kanjicapture.db.KanjiDatabase;
import org.cmacfarl.kanjicapture.db.Meaning;
import org.cmacfarl.kanjicapture.db.MeaningDao;
import org.cmacfarl.kanjicapture.db.Reading;
import org.cmacfarl.kanjicapture.db.ReadingDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class KanjiResultsActivity extends AppCompatActivity
{
    private static KanjiDatabase db = null;

    public static KanjiDatabase getKanjiDatabase()
    {
        if (db == null) {
            db = Room.databaseBuilder(KanjiCaptureApplication.getAppContext(), KanjiDatabase.class, "kanjidic")
                .fallbackToDestructiveMigration()
                .createFromAsset("kanjidic.db")
                .allowMainThreadQueries()
                .build();

        }
        return db;
    }

    public void populateResults(ArrayList<String> kanjis)
    {
        ReadingDao readingDao = getKanjiDatabase().readingDao();
        MeaningDao meaningDao = getKanjiDatabase().meaningDao();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.kanji_results_list, R.id.item_line, kanjis) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(R.id.item_line);
                TextView text2 = (TextView) view.findViewById(R.id.reading_line);
                TextView text3 = (TextView) view.findViewById(R.id.meaning_line);

                String kanji = kanjis.get(position);

                List<Reading> readings = readingDao.getReadings(kanji);
                List<Meaning> meanings = meaningDao.getMeanings(kanji);

                String mainLine = kanji;
                String readingStrings = readings.stream().map(e -> e.reading).collect(Collectors.joining(", "));
                String meaningStrings = meanings.stream().map(e -> e.meaning).collect(Collectors.joining(", "));
                text1.setText(mainLine);
                text2.setText(readingStrings);
                text3.setText(meaningStrings);
                return view;
            }
        };

        ListView listView = findViewById(R.id.results_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                final String item = kanjis.get(position);

                Intent i = new Intent(Intent.ACTION_VIEW);
                String url = "https://jisho.org/search/" + item;
                Uri uri = Uri.parse(url).normalizeScheme();
                i.setData(uri);
                startActivity(i);
            }

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra(KanjiCaptureActivity.EXTRA_KANJIS);
        ArrayList<String> object = (ArrayList<String>) args.getSerializable(KanjiCaptureActivity.KANJI_LIST);
        populateResults(object);
    }

}
