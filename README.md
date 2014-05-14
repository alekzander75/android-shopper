android-shopper
===============

TODO:

fix mode state bug.
Save mode state even when app is destroyed?

remove_all_items confirm dialog.

Improve export-import data functions.
	App data folders?
		https://developers.google.com/drive/web/appdata
	Switch to use a "generic" file chooser (even if the user must install another app).
		Maybe check if that exists and if not then use the FileExplorer lib intent.

Drag-n-drop to reorder items.

Slide left to delete item.

Use the support library to support a lower api level.

Increase targetSdkVersion and reduce minSdkVersion as much as possible / convenient:
	http://android-developers.blogspot.com.ar/2010/07/how-to-have-your-cupcake-and-eat-it-too.html
	https://stackoverflow.com/questions/4568267/android-min-sdk-version-vs-target-sdk-version
	https://developer.android.com/guide/topics/manifest/uses-sdk-element.html

UI/Application Exerciser Monkey
https://developer.android.com/tools/help/monkey.html

Pocket mode: Don't go to sleep in shopping mode?
