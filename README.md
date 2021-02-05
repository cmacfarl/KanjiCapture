# KanjiCapture

Android application for image recognition of Japanese kanji characters.  

This application differs from Google Translate in that it's focused on kanji recognition instead of sentence translation and gives the readings in kana instead of romaji.  Designed as a streamlined learning aid for kanji where the source material is printed and there is no furigana.

![diagram](https://raw.githubusercontent.com/cmacfarl/KanjiCapture/images/images/readme-diagram.png)

## Requirements

This application relies upon Google's Cloud Vision API.  Cloud vision usage and pricing tiers are described [here](https://cloud.google.com/vision/pricing).  Each account is allocated 1000 free queries a month. 

To build and use this application you must set up the Vision API within your own Google account as described at https://cloud.google.com/vision/docs/setup.  

## Acknowledgements

Uses [KANJIDIC2](http://www.edrdg.org/wiki/index.php/KANJIDIC_Project) for readings and meanings on the results activity.
