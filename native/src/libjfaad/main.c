#include "main.h"
#include <stdio.h>
#include <stdlib.h>

#include <neaacdec.h>
#include <mp4ff.h>

typedef struct {
	int numSamples;
	int track;
	unsigned char channels;
	uint32_t samplerate;
	mp4ff_t *infile;

	int currentSample;

	NeAACDecHandle hDecoder;
	NeAACDecFrameInfo *frameInfo;

	FILE *mp4File;
	mp4ff_callback_t *mp4cb;

	int offset;
} MP4FileHandler;

int GetAACTrack(mp4ff_t *infile) {
	/* find AAC track */
	int i, rc;
	int numTracks = mp4ff_total_tracks(infile);

	for (i = 0; i < numTracks; i++) {
		uint8_t *buff = NULL;
		unsigned int buff_size = 0;
		mp4AudioSpecificConfig mp4ASC;

		mp4ff_get_decoder_config(infile, i, &buff, &buff_size);

		if (buff) {
			rc = NeAACDecAudioSpecificConfig(buff, buff_size, &mp4ASC);
			free(buff);

			if (rc < 0)
				continue;
			return i;
		}
	}

	/* can't decode this */
	return -1;
}

uint32_t read_callback(void *user_data, void *buffer, uint32_t length) {
	return fread(buffer, 1, length, (FILE*) user_data);
}

uint32_t seek_callback(void *user_data, uint64_t position) {
	return fseek((FILE*) user_data, position, SEEK_SET);
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_open(
		JNIEnv * env, jobject obj, jstring fileName) {
	MP4FileHandler* handler = malloc(sizeof(MP4FileHandler));

	unsigned char *buffer;
	unsigned int buffer_size;

	/* initialise the callback structure */
	handler->mp4cb = malloc(sizeof(mp4ff_callback_t));

	const char *fname = (*env)->GetStringUTFChars(env, fileName, 0);
	//	printf("Opened file %s\n", fname);
	handler->mp4File = fopen(fname, "rb");
	(*env)->ReleaseStringUTFChars(env, fileName, fname);
	handler->mp4cb->read = read_callback;
	handler->mp4cb->seek = seek_callback;
	handler->mp4cb->user_data = handler->mp4File;

	handler->infile = mp4ff_open_read(handler->mp4cb);
	if (!handler->infile) {
		/* unable to open file */
		printf("Error opening file: %s\n", fname);
		return -1;
	}

	if ((handler->track = GetAACTrack(handler->infile)) < 0) {
		printf("Unable to find correct AAC sound track in the MP4 file.\n");
		mp4ff_close(handler->infile);
		free(handler->mp4cb);
		fclose(handler->mp4File);
		return -1;
	}

	buffer = NULL;
	buffer_size = 0;
	mp4ff_get_decoder_config(handler->infile, handler->track, &buffer,
			&buffer_size);

	handler->hDecoder = NeAACDecOpen();
	handler->frameInfo = malloc(sizeof(NeAACDecFrameInfo));

	if (NeAACDecInit2(handler->hDecoder, buffer, buffer_size,
			&handler->samplerate, &handler->channels) < 0) {
		/* If some error initializing occured, skip the file */
		printf("Error initializing decoder library.\n");
		NeAACDecClose(handler->hDecoder);
		mp4ff_close(handler->infile);
		free(handler->mp4cb);
		fclose(handler->mp4File);
		return -1;
	}
	if (buffer)
		free(buffer);

	handler->currentSample = 0;

	handler->numSamples = mp4ff_num_samples(handler->infile, handler->track);
	handler->offset = 0;

//	int i;
//	for (i = 0; i < handler->numSamples; i++) {
//		int tpf = mp4ff_get_sample_duration(handler->infile, handler->track, i);
//		long pos = mp4ff_get_sample_position(handler->infile, handler->track, i);
//		printf("Sample %d: Position %d Length %d\n", i, pos, tpf);
//	}
	//	printf("Total samples: %d\n", handler->numSamples);
	return (int) handler;
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_getSampleRate(
		JNIEnv * env, jobject obj, jint p_handler) {
	MP4FileHandler* handler = (MP4FileHandler*) p_handler;

	return handler->samplerate;
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_getChannels(
		JNIEnv * env, jobject obj, jint p_handler) {
	MP4FileHandler* handler = (MP4FileHandler*) p_handler;

	return handler->channels;
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_decode(
		JNIEnv * env, jobject obj, jint p_handler, jbyteArray samples,
		jint max) {
	MP4FileHandler* handler = (MP4FileHandler*) p_handler;

	//	printf("in decode\n");

	if (handler->currentSample >= handler->numSamples)
		return -1;
	unsigned char *buffer = NULL;
	char *sample_buffer;
	unsigned int buffer_size = 0;

	int rc = mp4ff_read_sample(handler->infile, handler->track,
			handler->currentSample, &buffer, &buffer_size);
	if (rc == 0) {
		//		NeAACDecClose(handler->hDecoder);
		//		mp4ff_close(handler->infile);
		//		free(handler->mp4cb);
		//		fclose(handler->mp4File);
		return -1;
	}

	//	printf("read sample %d bytes\n", buffer_size);

	sample_buffer = NeAACDecDecode(handler->hDecoder, handler->frameInfo,
			buffer, buffer_size);

	int len = handler->frameInfo->samples;
	//	printf("decoded: %d\n", len);
	jbyte *buf2 = (jbyte*) (sample_buffer + handler->offset * 4);
	//printf("Sample %d\n", handler->currentSample);
	//	printf("Samples %d %d\n", sample_buffer[1], buf2[0]);

	int tpf = handler->channels * mp4ff_get_sample_duration(handler->infile, handler->track, handler->currentSample);
	if (len > tpf) {
		len = tpf * 2;
	} else {
		len *= 2;
	}

	len -= handler->offset * 4;
//	printf("Length: %d\n", len);
	(*env)->SetByteArrayRegion(env, samples, 0, len, buf2);

	handler->offset = 0;

	if (buffer)
		free(buffer);
	handler->currentSample++;
	return len;
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_seek(
		JNIEnv * env, jobject object, jint p_handler, jlong smpl) {
	MP4FileHandler* handler = (MP4FileHandler*) p_handler;
	long sample = smpl + 1024 + 576;
	int i;

	for (i = 0; i < handler->numSamples; i++) {
		int64_t pos = mp4ff_get_sample_position(handler->infile,
				handler->track, i);
//		int tpf = mp4ff_get_sample_duration(handler->infile, handler->track, i);
		if (sample < pos)
			break;
	}

	handler->currentSample = i;
	int curSample = i - 2;
	if (curSample < 0) curSample = 0;
	unsigned char *buffer = NULL;
	unsigned int buffer_size = 0;

	for (i = 0; i < 2; i++) {
		mp4ff_read_sample(handler->infile, handler->track, curSample++,
				&buffer, &buffer_size);
		NeAACDecDecode(handler->hDecoder, handler->frameInfo, buffer,
				buffer_size);
	}

	int tpf = mp4ff_get_sample_duration(handler->infile, handler->track,
			0);
	int64_t pos = mp4ff_get_sample_position(handler->infile, handler->track,
			handler->currentSample);
	handler->offset = (sample - (pos - tpf));
	if (handler->offset < 0)
		handler->offset = 0;
	//	printf("Sample %d Offset: %d Tpf: %u Pos: %d Current Sample: %d\n", sample, handler->offset, tpf, pos, handler->currentSample);
//	printf("Tpf: %d ", tpf);
//	printf("Pos: %d ", pos);
//	printf("Current Sample: %d ", handler->currentSample);
//	printf("Offset: %d ", handler->offset);
//	printf("Seek to: %d ", sample);
//	printf("\n");

	if (buffer)
		free(buffer);

	//printf("Seek to: %d\n", i);
	//	NeAACDecPostSeekReset(handler->hDecoder, handler->currentSample);
	return 0;
}

JNIEXPORT jint JNICALL Java_com_tulskiy_musique_audio_formats_aac_libjfaad_libjfaad_close(
		JNIEnv * env, jobject object, jint p_handler) {
	MP4FileHandler* handler = (MP4FileHandler*) p_handler;

	NeAACDecClose(handler->hDecoder);
	free(handler->frameInfo);

	mp4ff_close(handler->infile);
	free(handler->mp4cb);
	fclose(handler->mp4File);

	free(handler);

	return 0;
}
