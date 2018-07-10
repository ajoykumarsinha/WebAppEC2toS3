/**
 * 
 */
package controller;

/**
 * @author ajoy
 *
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * Handles requests for the application file upload requests
 */
@Controller
public class FileUploadController {

	private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	UploadedFile ufile;

	public FileUploadController() {
		System.out.println("init RestController");
		ufile = new UploadedFile();
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String getHome(Model model) {
		model.addAttribute("home", "Hello world");

		return "index";
	}

	@RequestMapping(value = "/imageupload", method = RequestMethod.GET)
	public String getImageUpload(Model model) {
		model.addAttribute("imageupload", "Image Upload Page !!");

		return "imageupload";
	}

	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public @ResponseBody String upload(@RequestParam("userPhoto") MultipartFile uploadfile) {

		System.out.println(" file.getOriginalFilename() >> " + uploadfile.getOriginalFilename());

		try {

			System.out.println(" 1 >> " + uploadfile.getInputStream());
			System.out.println(" 2 >> " + uploadfile.getContentType());
			System.out.println(" 3 >> " + uploadfile.getSize());

		} catch (Exception ex) {

			ex.printStackTrace();

		}

		try {

			try {

				final AmazonS3 s3 = AmazonS3Client.builder()
		            	.withRegion(Regions.AP_SOUTH_1)
		    	        .withCredentials(new ProfileCredentialsProvider("ajoykumarsinha@gmail.com"))
		    	        .build();

				String bucket_name = "jistestbucket12345";
				String file_path = "";
				String key_name = uploadfile.getOriginalFilename().toString();

				InputStream is = uploadfile.getInputStream();
				byte[] contentBytes = IOUtils.toByteArray(is);
				Long contentLength = Long.valueOf(contentBytes.length);
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(contentLength);

				InputStream inputStream = uploadfile.getInputStream();

				s3.putObject(new PutObjectRequest(bucket_name, key_name, inputStream, metadata)
						.withCannedAcl(CannedAccessControlList.PublicRead));

			} catch (IOException e) {
				System.err.printf("Failed while reading bytes from %s", e.getMessage());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return "File has been uploaded sussessfully under bucket jistestbucket12345";

	}

	/**
	 * Upload multiple file using Spring Controller
	 */
	@RequestMapping(value = "/uploadMultipleFile", method = RequestMethod.POST)
	public @ResponseBody String uploadMultipleFileHandler(@RequestParam("name") String[] names,
			@RequestParam("file") MultipartFile[] files) {

		if (files.length != names.length)
			return "Mandatory information missing";

		String message = "";
		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			String name = names[i];
			try {
				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				String rootPath = System.getProperty("catalina.home");
				File dir = new File(rootPath + File.separator + "tmpFiles");
				if (!dir.exists())
					dir.mkdirs();

				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();

				logger.info("Server File Location=" + serverFile.getAbsolutePath());

				message = message + "You successfully uploaded file=" + name + "<br />";
			} catch (Exception e) {
				return "You failed to upload " + name + " => " + e.getMessage();
			}
		}
		return message;
	}
}