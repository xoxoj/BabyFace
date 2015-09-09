package org.faudroids.babyface.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.faudroids.babyface.R;
import org.faudroids.babyface.faces.FacesImportService;
import org.faudroids.babyface.faces.FacesScanner;
import org.faudroids.babyface.utils.DefaultTransformer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * Handles showing the import faces UI
 */
public class FacesImportViewHandler {

	private final FacesScanner scanner;

	@Inject
	FacesImportViewHandler(FacesScanner scanner) {
		this.scanner = scanner;
	}


	/**
	 * @param activity an activity which is capable of showing the progress bar
	 * @param importListener listener for import updates
	 * @param targetIntent which activity to launch once import is finished. Can be null.
	 */
	public void showImportDialog(final AbstractActivity activity, final ImportListener importListener, @Nullable final Intent targetIntent) {
		scanner.scanGoogleDriveForFaces()
				.compose(new DefaultTransformer<List<FacesScanner.ImportableFace>>())
				.subscribe(new Action1<List<FacesScanner.ImportableFace>>() {
					@Override
					public void call(final List<FacesScanner.ImportableFace> faces) {
						Timber.d("found " + faces.size() + " faces to import");
						for (FacesScanner.ImportableFace face : faces) {
							Timber.d(face.getFaceName() + " with " + face.getPhotos().size() + " images");
						}

						if (faces.isEmpty()) {
							importListener.onSuccess(ImportStatus.NOTHING_TO_IMPORT);
							return;
						}

						// ask user whether those faces should be "imported"
						new AlertDialog.Builder(activity)
								.setTitle(R.string.import_title)
								.setMessage(activity.getString(R.string.import_message, faces.size()))
								.setPositiveButton(R.string.import_confirm, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent activityIntent = new Intent(activity, FacesImportActivity.class);
										activityIntent.putExtra(FacesImportActivity.EXTRA_TARGET_INTENT, targetIntent);
										activity.startActivity(activityIntent);

										Intent serviceIntent = new Intent(activity, FacesImportService.class);
										serviceIntent.putParcelableArrayListExtra(FacesImportService.EXTRA_FACES_TO_IMPORT, new ArrayList<>(faces));
										activity.startService(serviceIntent);

										importListener.onSuccess(ImportStatus.IMPORT_STARTED);
									}
								})
								.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										importListener.onSuccess(ImportStatus.IMPORT_ABORTED);
									}
								})
								.show();

					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "error scanning for faces");
						importListener.onError(throwable);
					}
				});
	}


	public enum ImportStatus {
		/** Called when there were no faces to import */
		NOTHING_TO_IMPORT,

		/** Found when a number of faces have been imported */
		IMPORT_STARTED,

		/** When the user chose to abort the import process */
		IMPORT_ABORTED
	}

	public interface ImportListener {

		/**
		 * When the import was successfully handled.
		 */
		void onSuccess(ImportStatus status);

		/**
		 * When there was an error searching for faces.
		 */
		void onError(Throwable throwable);

	}

}
