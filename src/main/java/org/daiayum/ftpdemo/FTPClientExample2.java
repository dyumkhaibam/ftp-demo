/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.daiayum.ftpdemo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;

/**
 * This is an example program demonstrating how to use the FTPClient class. This
 * program connects to an FTP server and retrieves the specified file. If the -s
 * flag is used, it stores the local file at the FTP server. Just so you can see
 * what's happening, all reply strings are printed. If the -b flag is used, a
 * binary transfer is assumed (default is ASCII). See below for further options.
 */
public final class FTPClientExample2 {

	private static CopyStreamListener createListener() {
		return new CopyStreamListener() {
			private long megsTotal;

			@Override
			public void bytesTransferred(final CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
			}

			@Override
			public void bytesTransferred(final long totalBytesTransferred, final int bytesTransferred,
					final long streamSize) {
				final long megs = totalBytesTransferred / 1000000;
				for (long l = megsTotal; l < megs; l++) {
					System.err.print("#");
				}
				megsTotal = megs;
			}
		};
	}

	public static void mains(final String[] args) throws IOException {
		boolean storeFile = false, binaryTransfer = false, error = false, listFiles = false, listNames = false,
				hidden = false;
		boolean localActive = false, useEpsvWithIPv4 = false, feat = false, printHash = false;
		boolean mlst = false, mlsd = false, mdtm = false, saveUnparseable = false;
		boolean size = false;
		boolean lenient = false;
		long keepAliveTimeoutSeconds = -1;
		int controlKeepAliveReplyTimeoutMillis = -1;
		int minParams = 5; // listings require 3 params
		String protocol = null; // SSL protocol
		String doCommand = null;
		String trustmgr = null;
		String proxyHost = null;
		int proxyPort = 80;
		String proxyUser = null;
		String proxyPassword = null;
		String username = null;
		String password = null;
		String encoding = null;
		String serverTimeZoneId = null;
		String displayTimeZoneId = null;
		String serverType = null;
		String defaultDateFormat = null;
		String recentDateFormat = null;

		binaryTransfer = true;
		keepAliveTimeoutSeconds = 10 * 60;

		listFiles = true;
		protocol = "false";
		// serverType = args[++base];
		controlKeepAliveReplyTimeoutMillis = 10 * 60 * 1000;

		String server = "ftp.support.bonitasoft.com";
		int port = 21;

		username = "migration_csc";
		password = "UW@K>DMDJ$Y#l])%yb32";

		String remote = null;

		final FTPClient ftp;

		FTPSClient ftps = new FTPSClient(false);
		ftp = ftps;

		if (printHash) {
			ftp.setCopyStreamListener(createListener());
		}
		if (keepAliveTimeoutSeconds >= 0) {
			ftp.setControlKeepAliveTimeout(Duration.ofSeconds(keepAliveTimeoutSeconds));
		}
		if (controlKeepAliveReplyTimeoutMillis >= 0) {
			ftp.setControlKeepAliveReplyTimeout(Duration.ofMillis(controlKeepAliveReplyTimeoutMillis));
		}
		ftp.setListHiddenFiles(hidden);
		ftp.setDefaultTimeout(10*60*1000); // 10 minutes
		ftp.setConnectTimeout(10*60*1000);
        
		// suppress login details
		ftp.addProtocolCommandListener(new PrintCommandListener(System.out));

		try {
			System.out.println("Connecting...");
			final int reply;
			if (port > 0) {
				ftp.connect(server, port);
			} else {
				ftp.connect(server);
			}
			System.out.println("Connected to " + server + " on " + (port > 0 ? port : ftp.getDefaultPort()));

			// After connection attempt, you should check the reply code to verify
			// success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
		} catch (final IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (final IOException f) {
					// do nothing
				}
			}
			System.err.println("Could not connect to server.");
			e.printStackTrace();
			System.exit(1);
		}

		__main: try {
			if (!ftp.login(username, password)) {
				ftp.logout();
				error = true;
				break __main;
			}

			System.out.println("Remote system is " + ftp.getSystemType());

			if (binaryTransfer) {
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
			} else {
				// in theory this should not be necessary as servers should default to ASCII
				// but they don't all do so - see NET-500
				ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}

			// Use passive mode as default because most of us are
			// behind firewalls these days.
			if (localActive) {
				ftp.enterLocalActiveMode();
			} else {
				ftp.enterLocalPassiveMode();
			}

			ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

			if (storeFile) {
				try (final InputStream input = new FileInputStream("")) {
					ftp.storeFile(remote, input);
				}

				if (keepAliveTimeoutSeconds > 0) {
					showCslStats(ftp);
				}
			}
			// Allow multiple list types for single invocation
			else if (listFiles || mlsd || mdtm || mlst || listNames || size) {
				if (mlsd) {
					for (final FTPFile f : ftp.mlistDir(remote)) {
						System.out.println(f.getRawListing());
						System.out.println(f.toFormattedString(displayTimeZoneId));
					}
				}
				if (mdtm) {
					final FTPFile f = ftp.mdtmFile(remote);
					if (f != null) {
						System.out.println(f.getRawListing());
						System.out.println(f.toFormattedString(displayTimeZoneId));
					} else {
						System.out.println("File not found");
					}
				}
				if (mlst) {
					final FTPFile f = ftp.mlistFile(remote);
					if (f != null) {
						System.out.println(f.toFormattedString(displayTimeZoneId));
					}
				}
				if (listNames) {
					for (final String s : ftp.listNames(remote)) {
						System.out.println(s);
					}
				}
				if (size) {
					System.out.println("Size=" + ftp.getSize(remote));
				}
				// Do this last because it changes the client

				for (final FTPFile f : ftp.listFiles()) {
					System.out.println(f.getRawListing());
					System.out.println(f.toFormattedString(displayTimeZoneId));
				}
			}
		} finally {
			ftp.noop(); // check that control connection is working OK

			ftp.logout();

		}

	} // end main

	private static void showCslStats(final FTPClient ftp) {
		@SuppressWarnings("deprecation") // debug code
		final int[] stats = ftp.getCslDebug();
		System.out.println("CslDebug=" + Arrays.toString(stats));

	}
}