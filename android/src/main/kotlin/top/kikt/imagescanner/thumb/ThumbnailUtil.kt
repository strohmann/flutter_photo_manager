package top.kikt.imagescanner.thumb

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.Transition
import io.flutter.plugin.common.MethodChannel
import top.kikt.imagescanner.util.ResultHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Created by debuggerx on 18-9-27 下午2:08
 */
object ThumbnailUtil
{
	fun getThumbnailByGlide(ctx: Context, path: String, width: Int, height: Int, format: Int, quality: Int, result: MethodChannel.Result?)
	{
		val resultHandler = ResultHandler(result);

		val requestOptions = RequestOptions();
		if (format == 2)
			requestOptions.format(DecodeFormat.PREFER_ARGB_8888);

		Glide.with(ctx)
			.asBitmap()
			.disallowHardwareConfig()
			.load(File(path))
			.apply(requestOptions)
			.into(object : BitmapTarget(width, height)
			{
				override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
				{
					super.onResourceReady(resource, transition);

					if (format == 2)
					{
						var bitmap:Bitmap = resource;
						// Convert to ARGB
						if (resource.config != Bitmap.Config.ARGB_8888)
							bitmap = resource.copy(Bitmap.Config.ARGB_8888, false);

						val generatedWidth:Int = bitmap.width;
						val generatedHeight:Int = bitmap.height;

						val bufferSize = bitmap.byteCount + 8;
						val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize);
						buffer.putInt(generatedWidth);
						buffer.putInt(generatedHeight);

						val bufferBmp: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount);
						bitmap.copyPixelsToBuffer(bufferBmp);
						bufferBmp.rewind();
						buffer.put(bufferBmp);

						buffer.rewind();

						resultHandler.reply(buffer.array());
					}
					else
					{
						val bos = ByteArrayOutputStream();
						val compressFormat = if (format == 1)
						{
							Bitmap.CompressFormat.PNG
						}
						else
						{
							Bitmap.CompressFormat.JPEG
						};

						resource.compress(compressFormat, quality, bos);
						resultHandler.reply(bos.toByteArray());
					}
				}

				override fun onLoadCleared(placeholder: Drawable?)
				{
					resultHandler.reply(null);
				}

				override fun onLoadFailed(errorDrawable: Drawable?)
				{
					resultHandler.reply(null);
				}
			});
	}


	fun getThumbOfUri(context: Context, uri: Uri, width: Int, height: Int, format: Int, quality: Int, callback: (ByteArray?) -> Unit)
	{
		val requestOptions = RequestOptions();
		if (format == 2)
			requestOptions.format(DecodeFormat.PREFER_ARGB_8888);

		Glide.with(context)
			.asBitmap()
			.disallowHardwareConfig()
			.load(uri)
			.apply(requestOptions)
			.into(object : BitmapTarget(width, height)
			{
				override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
				{
					super.onResourceReady(resource, transition);


					if (format == 2)
					{
						var bitmap:Bitmap = resource;
						// Convert to ARGB
						if (resource.config != Bitmap.Config.ARGB_8888)
							bitmap = resource.copy(Bitmap.Config.ARGB_8888, false);

						val generatedWidth:Int = bitmap.width;
						val generatedHeight:Int = bitmap.height;

						val bufferSize = bitmap.byteCount + 8;
						val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize);
						buffer.putInt(generatedWidth);
						buffer.putInt(generatedHeight);

						val bufferBmp: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount);
						bitmap.copyPixelsToBuffer(bufferBmp);
						bufferBmp.rewind();
						buffer.put(bufferBmp);

						buffer.rewind();

						callback(buffer.array());
					}
					else
					{
						val bos = ByteArrayOutputStream();

						val compressFormat = if (format == 1)
						{
							Bitmap.CompressFormat.PNG
						}
						else
						{
							Bitmap.CompressFormat.JPEG
						};

						resource.compress(compressFormat, quality, bos);
						callback(bos.toByteArray());
					}
				}

				override fun onLoadCleared(placeholder: Drawable?)
				{
					callback(null);
				}

				override fun onLoadFailed(errorDrawable: Drawable?)
				{
					callback(null);
				}
			});
	}
}
