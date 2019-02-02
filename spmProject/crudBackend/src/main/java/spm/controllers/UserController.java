package spm.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.UsesJava7;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import spm.entities.Folder;
import spm.entities.PublicFolder;
import spm.entities.Search;
import spm.entities.ShareCode;
import spm.entities.BpmnModel;
import spm.entities.User;
import spm.entities.Version;
import spm.entities.Xmlfile;
import spm.repositories.PublicMongoRepository;
import spm.repositories.ShareCodesMongoRepository;
import spm.repositories.UserMongoRepository;
import spm.service.StorageService;
import spm.utilities.RandomGenerator;
import static java.nio.file.StandardCopyOption.*;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class UserController {

	@Autowired
	StorageService storageService;

	List<String> files = new ArrayList<String>();


	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserMongoRepository userRepository;

	@Autowired
	private PublicMongoRepository publicRepository;

	@Autowired
	private ShareCodesMongoRepository shareCodes;

	@GetMapping("/users")
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@PostMapping("/userLogin")
	public boolean loginUser(@RequestBody User user) {
		boolean success = false;
		List<User> users = userRepository.findAll();
		for (int i = 0; i < users.size(); i++) {
			if (((user.getUsername().equals(users.get(i).getUsername()))
					|| (user.getUsername().equals(users.get(i).getEmail())))
					&& (passwordEncoder.matches(user.getPassword(), users.get(i).getPassword()))) {
				success = true;
			}
		}
		return success;
	}

	@PostMapping("/user")
	public User createUser(@RequestBody User user) {
		List<User> users = userRepository.findAll();
		BigInteger folderId = BigInteger.valueOf(0);
		File f = new File(
				"C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + user.getUsername());
		f.mkdir();
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		List<Folder> folders = new ArrayList<Folder>();
		user.setFolders(folders);

		for (int i = 0; i < users.size(); i++) {
			if (folderId.compareTo(users.get(i).getFoldersId()) == -1) {
				folderId = users.get(i).getFoldersId();
			}
		}
		user.setModelsId(BigInteger.valueOf(0));
		user.setVersionsId(BigInteger.valueOf(0));
		user.setFoldersId(folderId);
		return userRepository.save(user);
	}

	@PostMapping("/userUpdate/{password:.+}")
	public boolean updateUser(@RequestBody User user, @PathVariable String password) {
		boolean exists = false;
		if (passwordEncoder.matches(password, user.getPassword())) {
			exists = true;
			return exists;
		} else {
			user.setPassword(passwordEncoder.encode(password));
			userRepository.save(user);
			return exists = false;
		}
	}

	@PostMapping("/createFolder/{folderName:.+}")
	public boolean createFolder(@RequestBody User user, @PathVariable String folderName) {
		boolean success = true;
		if (folderName.equalsIgnoreCase(""))
			return false;
		File f = new File(
				"C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + user.getUsername() + "\\" + folderName);
		BigInteger folderId = BigInteger.valueOf(0);
		String currentDate = getCurrentDate();
		List<ShareCode> codes = shareCodes.findAll();
		codes = shareCodes.findAll();
		for(int i = 0; i< codes.size(); i++) {
			System.out.println(codes.get(i).getShareCode());
		}

		List<User> users = userRepository.findAll();
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				List<Folder> userFolders = new ArrayList<Folder>();
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < userFolders.size(); j++) {
					if (folderName.equalsIgnoreCase(userFolders.get(j).getfName())) {
						success = false;
					}
				}
				// If the user exists and its list of folders are returned
				// create the folder with input string, set the folder to the user and update it
				// in database
				if (success) {
					user = users.get(i);
					folderId = user.getFoldersId().add(BigInteger.ONE);
					Folder folder = new Folder(folderId, folderName);
					// Creation of an empty list of folders
					List<Folder> folders = new ArrayList<Folder>();
					// Creation of an empty list of models
					List<BpmnModel> models = new ArrayList<BpmnModel>();
					folder.setFolders(folders);
					folder.setModels(models);
					folder.setCreationDate(currentDate);
					userFolders.add(folder);
					user.setFolders(userFolders);
					user.setFoldersId(folderId);
					userRepository.save(user);
				}
			}
		}
		for (int i = 0; i < users.size(); i++) {
			users.get(i).setFoldersId(folderId);
			userRepository.save(users.get(i));
		}
		f.mkdir();
		return success;
	}

	@PostMapping("/createInnerFolder/{currentFolder}+{createdName}")
	public boolean createInnerFolder(@RequestBody User user, @PathVariable String currentFolder,
			@PathVariable String createdName) {
		boolean success = true;
		if (createdName.equals(""))
			return false;
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<Folder> userInnerFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		String[] splittedFolders = currentFolder.split("&");
		BigInteger folderId = BigInteger.valueOf(0);


		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				folderId = user.getFoldersId().add(BigInteger.ONE);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				for (int j = 0; j < splittedFolders.length; j++) {
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j])) {
							folderLists.add(userFolders.get(k).getFolders());
							userFolders = userFolders.get(k).getFolders();
						}
					}
					if (j == splittedFolders.length - 1) {
						for (int k = 0; k < userFolders.size(); k++) {
							if (userFolders.get(k).getfName().equalsIgnoreCase(createdName)) {
								return false;
							}
						}
						Folder innerFolder = new Folder(folderId, createdName);

						List<Folder> folders = new ArrayList<Folder>();
						List<BpmnModel> models = new ArrayList<BpmnModel>();
						innerFolder.setFolders(folders);
						innerFolder.setModels(models);
						innerFolder.setCreationDate(getCurrentDate());
						userFolders.add(innerFolder);
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setFoldersId(user.getFoldersId().add(BigInteger.ONE));
				userRepository.save(user);
			}
		}
		for (int i = 0; i < users.size(); i++) {
			users.get(i).setFoldersId(folderId);
			userRepository.save(users.get(i));
		}
		String mergedPath = String.join("\\", splittedFolders);
		File f = new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + user.getUsername() + "\\"
				+ mergedPath + "\\" + createdName);
		f.mkdir();
		return success;
	}

	@PostMapping("/showFolders")
	public List<Folder> showFolders(@RequestBody User user) {
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				userFolders = users.get(i).getFolders();
			}
		}
		return userFolders;
	}

	@PostMapping("/showInnerFolders/{currentFolder}")
	public List<Folder> showInnerFolders(@RequestBody User user, @PathVariable String currentFolder) {
		System.out.println("showfolders");
		System.out.println(currentFolder);
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<Folder> foldersToShow = new ArrayList<Folder>();
		String[] splittedFolders = currentFolder.split("&");
		boolean found = false;

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j])) {
							if(!found) {
								userFolders = userFolders.get(k).getFolders();
								found = true;
							}
						}
					}
				}
			}
		}

		return userFolders;
	}

	@PostMapping("/renameFolder/{currentName:.+}+{newName:.+}+{folderPath:.+}+{renameType}")
	public boolean renameFolder(@RequestBody User user, @PathVariable String currentName, @PathVariable String newName,
			@PathVariable String folderPath, @PathVariable String renameType) {
		String currentPath;
		String newPath;
		String previousPath = "";
		boolean found = false;
		if (newName.equals(""))
			return false;
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<Folder> userInnerFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		String[] splittedFolders = folderPath.split("&");

		System.out.println("renamefolderPath: " + folderPath);
		if (!renameType.equals("root")) {
			if (!folderPath.equals("root")) {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + folderPath
						+ "&" + currentName;
				newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + folderPath
						+ "&" + newName;
			} else {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&"
						+ currentName;
				newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + newName;
			}
		} else {
			for (int i = 0; i < splittedFolders.length - 1; i++) {
				previousPath = previousPath.concat(splittedFolders[i] + "&");
			}
			currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + previousPath
					+ "&" + currentName;
			newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + previousPath + "&"
					+ newName;
		}

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				if (!folderPath.equals("root")) {
					for (int j = 0; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < userFolders.size(); k++) {
							if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								if (renameType.equals("root")) {
									if (j == splittedFolders.length - 1) {
										for (int y = 0; y < userFolders.size(); y++) {
											if (userFolders.get(y).getfName().equalsIgnoreCase(newName)) {
												return false;
											}
										}
										for (int y = 0; y < userFolders.size(); y++) {
											if (userFolders.get(y).getfName().equalsIgnoreCase(currentName)) {
												userFolders.get(y).setfName(newName);
											}
										}
									}
									folderLists.add(userFolders.get(k).getFolders());
									userFolders = userFolders.get(k).getFolders();
								} else if (renameType.equals("child")) {
									folderLists.add(userFolders.get(k).getFolders());
									userFolders = userFolders.get(k).getFolders();
									if (j == splittedFolders.length - 1) {
										for (int y = 0; y < userFolders.size(); y++) {
											if (userFolders.get(y).getfName().equalsIgnoreCase(newName)) {
												return false;
											}
										}
										for (int y = 0; y < userFolders.size(); y++) {
											if (userFolders.get(y).getfName().equalsIgnoreCase(currentName)) {
												userFolders.get(y).setfName(newName);
											}
										}
									}
								}
							}
						}
					}
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(userFolders);
					}
					user.setFolders(folderLists.get(0));
				} else {
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equalsIgnoreCase(newName))
							return false;
					}
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equalsIgnoreCase(currentName))
							userFolders.get(k).setfName(newName);
					}
				}
				userRepository.save(user);
			}
		}
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		return true;
	}

	@PostMapping("/showModels/{currentFolder}")
	public List<BpmnModel> showModels(@RequestBody User user, @PathVariable String currentFolder) {
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<BpmnModel> model = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsFiltered = new ArrayList<BpmnModel>();
		String[] splittedFolders = currentFolder.split("&");
		String[] splittedModel;
		boolean found = false;
		String modelName;
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								model = userFolders.get(k).getModels();
								for(int y = 0; y<model.size(); y++) {
									modelName = model.get(y).getmName();
									splittedModel = modelName.split("-");


									if(splittedModel.length>1 && splittedModel[1].equalsIgnoreCase("version")) {

									}
									else
										modelsFiltered.add(model.get(y));

								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
			}
		}
		return modelsFiltered;
	}

	@PostMapping("/renameModel/{currentName:.+}+{newName:.+}+{modelPath:.+}")
	public boolean renameModel(@RequestBody User user, @PathVariable String currentName, @PathVariable String newName,
			@PathVariable String modelPath) {
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = modelPath.split("&");
		boolean found = false;
		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_"
				+ user.getUsername() + "&" + modelPath + "&" + currentName + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + modelPath + "&" + newName + ".bpmn";
		if (newName.isEmpty() || newName == null)
			return false;

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								folderLists.add(userFolders.get(k).getFolders());
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									System.out.println(folderModels.get(y).getmName());
									if (folderModels.get(y).getmName().equalsIgnoreCase(newName)) {
										return false;
									}
								}
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(currentName)) {
										folderModels.get(y).setmName(newName);
										userFolders.get(k).setModels(folderModels);
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}

					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				userRepository.save(user);
			}
		}
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		return true;
	}

	@PostMapping("/moveModel/{newPath:.+}+{modelName:.+}+{currentPath:.+}")
	public boolean moveModel(@RequestBody User user, @PathVariable String newPath, @PathVariable String modelName,
			@PathVariable String currentPath) {



		boolean found = false;
		BpmnModel modelToMove = new BpmnModel();
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<Folder> userFolders2 = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<List<Folder>> folderLists2 = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<BpmnModel> folderModels2 = new ArrayList<BpmnModel>();
		String dbCurrentLocation = currentPath;
		String modelNameTxt;
		BigInteger modelId = BigInteger.valueOf(0);
		String[] splittedFolders = dbCurrentLocation.split("&");
		String[] splittedFolders2 = newPath.split("&");
		currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + currentPath;
		// Move the model locally
		boolean success = false;
		if (newPath.equals(""))
			return success;
		modelNameTxt = modelName.concat(".bpmn");
		newPath = newPath.concat("&" + modelNameTxt);
		newPath = newPath.replace("&", "\\");
		currentPath = currentPath.replace("&", "\\").concat("\\" + modelNameTxt);
		File model = new File(currentPath);
		model.renameTo(new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_"
				+ user.getUsername() + "\\" + newPath));
		// move the database reference



		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());

				userFolders2 = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders2.length; j++) {
					found = false;
					for (int k = 0; k < userFolders2.size(); k++) {
						if (userFolders2.get(k).getfName().equals(splittedFolders2[j]) && !found) {
							found = true;
							if (j == splittedFolders2.length - 1) {
								BpmnModel movedModel = new BpmnModel(modelId, modelName);
								List<BpmnModel> models = new ArrayList<BpmnModel>();
								models = userFolders2.get(k).getModels();

								for(int y = 0;y<models.size();y++) {
									if(movedModel.getmName().equalsIgnoreCase(models.get(y).getmName())) {
										return false;
									}
								}
							}
							userFolders2 = userFolders2.get(k).getFolders();
						}
					}
				}

				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							folderLists.add(userFolders.get(k).getFolders());
							if (j == splittedFolders.length - 1) {
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(modelName)) {
										modelToMove = folderModels.get(y);
										modelId = folderModels.get(y).getmId();
										folderModels.remove(y);
										userFolders.get(k).setModels(folderModels);
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				userRepository.save(user);
			}
		}

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders2 = users.get(i).getFolders();
				folderLists2.add(user.getFolders());
				for (int j = 0; j < splittedFolders2.length; j++) {
					found = false;
					for (int k = 0; k < userFolders2.size(); k++) {
						if (userFolders2.get(k).getfName().equals(splittedFolders2[j]) && !found) {
							found = true;
							folderLists2.add(userFolders2.get(k).getFolders());
							if (j == splittedFolders2.length - 1) {
								BpmnModel movedModel = new BpmnModel();
								movedModel = modelToMove;
								List<BpmnModel> models = new ArrayList<BpmnModel>();
								models = userFolders2.get(k).getModels();

								models.add(movedModel);
								userFolders2.get(k).setModels(models);
							}
							userFolders2 = userFolders2.get(k).getFolders();
						}
					}
				}
				for (int j = folderLists2.size() - 1; j <= 0; j--) {
					folderLists2.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists2.get(0));
				userRepository.save(user);
			}
		}
		success = true;
		return success;
	}

	@PostMapping("/checkFolderPath/{folderPath}")
	public boolean checkFolderPath(@RequestBody User user, @PathVariable String folderPath) {
		boolean exists = false;
		int nextFolder = 0;
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<Folder> foldersToShow = new ArrayList<Folder>();
		String[] splittedFolders = folderPath.split("&");
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders.length; j++) {
					for (int k = 0; k < userFolders.size(); k++) {

						if (userFolders.get(k).getfName().equals(splittedFolders[j])) {
							exists = true;
							nextFolder = k;
						}
					}
					if (!exists)
						return false;

					if (j < splittedFolders.length - 1) {
						userFolders = userFolders.get(nextFolder).getFolders();
						exists = false;
					}
				}
				if (exists)
					return true;
			}
		}
		return false;
	}

	@PostMapping("/addVersionService/{selectedModel:.+}+{versionDescription:.+}+{modelPath:.+}")
	public boolean addVersionService(@RequestBody User user, @PathVariable String selectedModel,
			@PathVariable String versionDescription, @PathVariable String modelPath) throws IOException {


		System.out.println("versionDescription: " + versionDescription);
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = modelPath.split("&");
		int getPreviousVersion = 0, getModelNumber = 0;
		boolean found = false;
		boolean versionAdded = false;
		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());

		String randomString = gen.nextString();


		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				System.out.println("checkpoint1");
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							System.out.println("checkpoint2");
							found = true;
							if(j == splittedFolders.length - 1) {
								folderLists.add(userFolders.get(k).getFolders());
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(selectedModel) && !versionAdded) {
										versionAdded = true;
										System.out.println("checkpoint3");
										BpmnModel model = new BpmnModel(user.getModelsId().add(BigInteger.ONE), selectedModel);
										getPreviousVersion = folderModels.get(y).getVersionNumber();
										getModelNumber = folderModels.get(y).getModelNumber();
										model.setShareCode(randomString);
										model.setModelNumber(folderModels.get(y).getModelNumber());
										model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
										model.setVersionDescription(versionDescription);
										model.setCreationDate(getCurrentDate());
										model.setAuthor(user.getUsername());
										folderModels.add(model);
										folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + selectedModel);
										userFolders.get(k).setModels(folderModels);
										System.out.println("checkpoint4");
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}

					}
				}
				System.out.println("checkpoint5");
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setModelsId(user.getModelsId().add(BigInteger.ONE));
				userRepository.save(user);
				System.out.println("checkpoint6");
			}
		}

		System.out.println("checkpoint7");


		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_"
				+ user.getUsername() + "&" + modelPath + "&" + selectedModel + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + modelPath + "&m" + getModelNumber + "-version-" + getPreviousVersion + "-" + selectedModel + ".bpmn";
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		Path source = Paths.get(newPath);
		Path dest = Paths.get(currentPath);
		Files.copy(source, dest, LinkOption.NOFOLLOW_LINKS);

		return true;
	}

	@PostMapping("/showVersionsService/{selectedModel:.+}+{modelPath:.+}")
	public List<BpmnModel> showVersionsService(@RequestBody User user, @PathVariable String selectedModel,
			@PathVariable String modelPath) {


		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		List<BpmnModel> modelVersions = new ArrayList<BpmnModel>();
		int modelNumber = 0;
		List<Version> versions = new ArrayList<Version>();
		String[] splittedFolders = modelPath.split("&");

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				userFolders = users.get(i).getFolders();
				for (int y = 0; y < splittedFolders.length; y++) {
					for (int j = 0; j < userFolders.size(); j++) {
						if (userFolders.get(j).getfName().equals(splittedFolders[y])) {
							if (y == splittedFolders.length - 1) {
								models = userFolders.get(j).getModels();
								for (int k = 0; k < models.size(); k++) {
									if (models.get(k).getmName().equals(selectedModel)) {
										modelNumber = models.get(k).getModelNumber();
									}
								}
								for (int k = 0; k < models.size(); k++) {
									if (models.get(k).getModelNumber() == modelNumber) {
										modelVersions.add(models.get(k));
									}
									if(k == models.size()-1) {
										return modelVersions;
									}
								}
							}
							userFolders = userFolders.get(j).getFolders();
						}
					}
				}
			}
		}

		return null;
	}


	@PostMapping("/deleteFolderService/{selectedFolder:.+}+{folderPath:.+}+{deleteType:.+}")
	public boolean deleteFolderService(@RequestBody User user, @PathVariable String selectedFolder,
			@PathVariable String folderPath, @PathVariable String deleteType) throws IOException {

		System.out.println("deletefolder");

		String currentPath = "";
		String previousPath = "";
		boolean removed = false;
		boolean found = false;
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");


		if (!deleteType.equals("root")) {
			if (!folderPath.equals("root")) {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + folderPath
						+ "&" + selectedFolder;
			} else {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&"
						+ selectedFolder;
			}
		} else {
			for (int i = 0; i < splittedFolders.length - 1; i++) {
				previousPath = previousPath.concat(splittedFolders[i] + "&");
			}
			currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + previousPath
					+ "&" + selectedFolder;
		}


		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());

				if(deleteType.equals("root") && splittedFolders.length == 1) {
					currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername() + "&" + selectedFolder;
					for(int j = 0; j<userFolders.size(); j++) {
						if(userFolders.get(j).getfName().equals(selectedFolder)) {
							userFolders.remove(userFolders.get(j));
							user.setFolders(userFolders);
							userRepository.save(user);
							currentPath = currentPath.replace("&", "\\");
							FileUtils.deleteDirectory(new File(currentPath));

						}
					}
				}
				else {
					if (!folderPath.equals("root")) {
						for (int j = 0; j < splittedFolders.length; j++) {
							found = false;
							for (int k = 0; k < userFolders.size(); k++) {
								if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
									found = true;
									if (deleteType.equals("root")) {
										if (j == splittedFolders.length - 1) {
											for (int y = 0; y < userFolders.size(); y++) {
												if (userFolders.get(y).getfName().equalsIgnoreCase(selectedFolder)) {
													userFolders.remove(y);
													removed = true;
												}
											}
										}
										if(!removed) {
											folderLists.add(userFolders.get(k).getFolders());
											userFolders = userFolders.get(k).getFolders();
										}
									} else if (deleteType.equals("child")) {
										folderLists.add(userFolders.get(k).getFolders());
										userFolders = userFolders.get(k).getFolders();
										if (j == splittedFolders.length - 1) {
											for (int y = 0; y < userFolders.size(); y++) {
												if (userFolders.get(y).getfName().equalsIgnoreCase(selectedFolder)) {
													userFolders.remove(y);
												}
											}
										}
									}
								}
							}
						}
						for (int j = folderLists.size() - 1; j <= 0; j--) {
							folderLists.get(j).get(0).setFolders(userFolders);
						}
						user.setFolders(folderLists.get(0));
					} else {

						for (int k = 0; k < userFolders.size(); k++) {
							if (userFolders.get(k).getfName().equalsIgnoreCase(selectedFolder))
								userFolders.remove(k);
						}
					}

					currentPath = currentPath.replace("&", "\\");
					FileUtils.deleteDirectory(new File(currentPath));
					userRepository.save(user);
				}
			}
		}

		return true;
	}




	@PostMapping("/deleteModelService/{selectedModel:.+}+{modelPath:.+}")
	public boolean deleteModelService(@RequestBody User user, @PathVariable String selectedModel,
			@PathVariable String modelPath) {
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String dbCurrentLocation = modelPath;
		String modelNameTxt;
		boolean found = false;
		BigInteger modelId = BigInteger.valueOf(0);
		String[] splittedFolders = dbCurrentLocation.split("&");
		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + modelPath + "&" + selectedModel + ".bpmn";

		currentPath = currentPath.replace("&", "\\");
		File model = new File(currentPath);
		model.delete();

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								folderLists.add(userFolders.get(k).getFolders());
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(selectedModel)) {
										modelId = folderModels.get(y).getmId();
										folderModels.remove(y);
										userFolders.get(k).setModels(folderModels);
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				userRepository.save(user);
			}
		}
		return true;
	}


	@PostMapping("/mail/{password:.+}")
	public void sendMail(@RequestBody User user, @PathVariable String password) throws MessagingException {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("spm.project.unicam@gmail.com");
		mailSender.setPassword("spmproject1819");
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.debug", "true");
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		try {
			helper.setTo(user.getEmail());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		try {
			helper.setSubject("Password Recovery");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		try {
			helper.setText("Your recovery password is: " + password);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		mailSender.send(message);
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
	}

	@PostMapping("/uploadFileService/{folderPath:.+}+{fileName:.+}")
	public boolean uploadFileService(@RequestBody User user, @PathVariable String folderPath, @PathVariable String fileName) {

		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");
		String[] splittedFile = fileName.split("\\.");
		BigInteger modelId = BigInteger.valueOf(0);
		boolean found = false;

		String currentDate = getCurrentDate();

		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());

		String randomString = gen.nextString();

		fileName = "";
		for(int i = 0; i<splittedFile.length - 1; i++) {
			fileName = fileName.concat(splittedFile[i]);
		}


		for(int i = 0; i<users.size(); i++) {
			if(users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				modelId = user.getModelsId();
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								folderModels = userFolders.get(k).getModels();
								for(int y=0 ; y<folderModels.size(); y++) {
									if(folderModels.get(y).getmName().equalsIgnoreCase(fileName)) {
										return false;
									}
								}

								BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
								model.setShareCode(randomString);
								model.setVersionNumber(1);
								model.setVersionDescription("First version");
								model.setModelNumber(user.getModelsId().intValue());
								model.setCreationDate(currentDate);
								model.setAuthor(user.getUsername());
								folderModels.add(model);
								userFolders.get(k).setModels(folderModels);
							}

							folderLists.add(userFolders.get(k).getFolders());
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setModelsId(user.getModelsId().add(BigInteger.ONE));
				userRepository.save(user);
			}
		}
		for (int i = 0; i < users.size(); i++) {
			userRepository.save(users.get(i));
		}

		return true;
	}


	@PostMapping("/searchElements/{searchedElement:.+}+{searchFilter:.+}")
	public Search searchElements(@RequestBody User user, @PathVariable String searchedElement, @PathVariable String searchFilter) {

		System.out.println("Searched Element: " + searchedElement);
		System.out.println("Search Filter: " + searchFilter);

		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<Folder> searchedFolders = new ArrayList<Folder>();
		List<BpmnModel> searchedModels = new ArrayList<BpmnModel>();
		List<Integer> listCounter = new ArrayList<Integer>();
		List<Boolean> checked = new ArrayList<Boolean>();
		Search search = new Search();
		List<String> folderPath = new ArrayList<String>();
		List<String> folderPathList = new ArrayList<String>();
		String folderPathString = "";
		String modelPathString = "";
		List<String> modelPath = new ArrayList<String>();
		List<String> modelPathList = new ArrayList<String>();
		String tempModelName[];

		System.out.println(searchedElement);
		//List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		//String[] splittedFolders = folderPath.split("&");
		int i,j,k,y = 0;
		for(i = 0; i<users.size(); i++) {
			if(users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = user.getFolders();
				folderLists.add(user.getFolders());
				for(j = 0; j<userFolders.size(); j++) {
					folderModels = userFolders.get(j).getModels();
					for(k = 0; k<folderModels.size();k++) {
						if(searchFilter.equalsIgnoreCase("1")) {
							tempModelName = folderModels.get(k).getmName().split("-");
							if(folderModels.get(k).getmName().equalsIgnoreCase(searchedElement)) {
								if(tempModelName.length > 1) {
									if(!tempModelName[1].equalsIgnoreCase("version")) {
										modelPath.add(userFolders.get(j).getfName());

										modelPathList.add(userFolders.get(j).getfName());
										modelPathString = "";
										searchedModels.add(folderModels.get(k));
									}
								}
								else {
									modelPath.add(userFolders.get(j).getfName());

									modelPathList.add(userFolders.get(j).getfName());
									modelPathString = "";
									searchedModels.add(folderModels.get(k));
								}
								System.out.println("model added!");
							}
						}
						else if(searchFilter.equalsIgnoreCase("3")) {
							tempModelName = folderModels.get(k).getmName().split("-");
							if(folderModels.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
								if(tempModelName.length > 1) {
									if(!tempModelName[1].equalsIgnoreCase("version")) {
										modelPath.add(userFolders.get(j).getfName());

										modelPathList.add(userFolders.get(j).getfName());
										modelPathString = "";
										searchedModels.add(folderModels.get(k));
									}
								}
								else {
									modelPath.add(userFolders.get(j).getfName());

									modelPathList.add(userFolders.get(j).getfName());
									modelPathString = "";
									searchedModels.add(folderModels.get(k));
								}
								System.out.println("model added!");
							}
						}

					}
					if(searchFilter.equalsIgnoreCase("1")) {
						if(userFolders.get(j).getfName().equalsIgnoreCase(searchedElement)) {
							searchedFolders.add(userFolders.get(j));
							System.out.println("folder added!");

							for(int s = 0; s<folderPath.size(); s++) {
								if(s < folderPath.size()-1)
									folderPathString = folderPathString.concat(folderPath.get(s) + "/");
								else
									folderPathString = folderPathString.concat(folderPath.get(s));
							}
							folderPathString = "root";
							folderPathList.add(folderPathString);
							folderPathString = "";
							System.out.println("folder path: " + folderPath);

						}
					}
					else if(searchFilter.equalsIgnoreCase("3")) {
						System.out.println(userFolders.get(j).getCreationDate());
						if(userFolders.get(j).getCreationDate().equalsIgnoreCase(searchedElement)) {
							searchedFolders.add(userFolders.get(j));
							System.out.println("folder added!");

							for(int s = 0; s<folderPath.size(); s++) {
								if(s < folderPath.size()-1)
									folderPathString = folderPathString.concat(folderPath.get(s) + "/");
								else
									folderPathString = folderPathString.concat(folderPath.get(s));
							}
							folderPathString = "root";
							folderPathList.add(folderPathString);
							folderPathString = "";
							System.out.println("folder path: " + folderPath);

						}
					}

				}

				j=0;
				while(userFolders.get(j).getFolders().isEmpty() && j < userFolders.size() - 1) {
					j++;

				}


				if(!userFolders.get(j).getFolders().isEmpty()) {

					checked.add(true);
					listCounter.add(j);
					y++;
					modelPath.add(userFolders.get(j).getfName());
					folderPath.add(userFolders.get(j).getfName());
					folderLists.add(userFolders.get(j).getFolders());
					userFolders = userFolders.get(j).getFolders();

					System.out.println("not null, got next folder list!");
					listCounter.add(0);
					checked.add(false);
				}

				for(j = 0; j<listCounter.size();j++) {
					System.out.println("listcounter: " + listCounter.get(j));
				}
				//System.out.println("previous folderlist size: " + folderLists.get(y-1).size() );

				//If at least an inner folder has been found
				if(y != 0) {
					while(listCounter.get(y-1) < folderLists.get(y-1).size() - 1 || listCounter.get(0) < folderLists.get(0).size()) {
						System.out.println("start the while in which i loop until the previous folder is not complete");

						if(checked.get(y))
							listCounter.set(y,listCounter.get(y)+1);
						j = listCounter.get(y);
						while(j<userFolders.size()) {
							if(!userFolders.get(j).getFolders().isEmpty()) {
								if(!checked.get(y)) {

									for(k = 0; k<userFolders.size(); k++) {
										folderModels = userFolders.get(k).getModels();
										for(int x = 0; x < folderModels.size(); x++) {
											if(searchFilter.equalsIgnoreCase("1")) {
												tempModelName = folderModels.get(x).getmName().split("-");
												if(folderModels.get(x).getmName().equalsIgnoreCase(searchedElement)) {
													if(tempModelName.length > 1) {
														if(!tempModelName[1].equalsIgnoreCase("version")) {
															modelPath.add(userFolders.get(k).getfName());
															folderPathString = "";
															for(int s = 0; s<folderPath.size(); s++) {
																folderPathString = folderPathString.concat(folderPath.get(s) + "/");
															}
															folderPathString = folderPathString.concat(userFolders.get(k).getfName());
															modelPathList.add(folderPathString);
															modelPathString = "";
															folderPathString = "";
															searchedModels.add(folderModels.get(x));
														}
													}
													else {
														modelPath.add(userFolders.get(k).getfName());
														folderPathString = "";
														for(int s = 0; s<folderPath.size(); s++) {
															folderPathString = folderPathString.concat(folderPath.get(s) + "/");
														}
														folderPathString = folderPathString.concat(userFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														modelPathString = "";
														folderPathString = "";
														searchedModels.add(folderModels.get(x));
													}
													System.out.println("model added!");
												}
											}
											else if(searchFilter.equalsIgnoreCase("3")) {
												tempModelName = folderModels.get(x).getmName().split("-");
												if(folderModels.get(x).getCreationDate().equalsIgnoreCase(searchedElement)) {
													if(tempModelName.length > 1) {
														if(!tempModelName[1].equalsIgnoreCase("version")) {
															modelPath.add(userFolders.get(k).getfName());
															folderPathString = "";
															for(int s = 0; s<folderPath.size(); s++) {
																folderPathString = folderPathString.concat(folderPath.get(s) + "/");
															}
															folderPathString = folderPathString.concat(userFolders.get(k).getfName());
															modelPathList.add(folderPathString);
															modelPathString = "";
															folderPathString = "";
															searchedModels.add(folderModels.get(x));
														}
													}
													else {
														modelPath.add(userFolders.get(k).getfName());
														folderPathString = "";
														for(int s = 0; s<folderPath.size(); s++) {
															folderPathString = folderPathString.concat(folderPath.get(s) + "/");
														}
														folderPathString = folderPathString.concat(userFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														modelPathString = "";
														folderPathString = "";
														searchedModels.add(folderModels.get(x));
													}
													System.out.println("model added!");
												}
											}
										}
										System.out.println("foldername1: " + userFolders.get(k).getfName());
										if(searchFilter.equalsIgnoreCase("1")) {
											if(userFolders.get(k).getfName().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(userFolders.get(k));
												folderPathString = "";
												System.out.println("folder added!");
												for(int s = 0; s<folderPath.size(); s++) {
													if(s < folderPath.size()-1)
														folderPathString = folderPathString.concat(folderPath.get(s) + "/");
													else
														folderPathString = folderPathString.concat(folderPath.get(s));
												}
												folderPathList.add(folderPathString);
												folderPathString = "";
												System.out.println("folder path: " + folderPath);	
											}
										}
										else if(searchFilter.equalsIgnoreCase("3")) {
											if(userFolders.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(userFolders.get(k));
												folderPathString = "";
												System.out.println("folder added!");
												for(int s = 0; s<folderPath.size(); s++) {
													if(s < folderPath.size()-1)
														folderPathString = folderPathString.concat(folderPath.get(s) + "/");
													else
														folderPathString = folderPathString.concat(folderPath.get(s));
												}
												folderPathList.add(folderPathString);
												folderPathString = "";
												System.out.println("folder path: " + folderPath);	
											}
										}
									}
									checked.set(y, true);

								}

								checked.add(false);
								listCounter.set(y,j);
								folderPath.add(userFolders.get(j).getfName());
								modelPath.add(userFolders.get(j).getfName());
								folderLists.add(userFolders.get(j).getFolders());
								userFolders = userFolders.get(j).getFolders();
								listCounter.add(0);
								y++;
								//System.out.println("Y: " + y);
								for(k = 0; k<listCounter.size();k++) {
									System.out.println("listcounter: " + listCounter.get(k));
								}
								j = 0;
							}

							else {
								if(!checked.get(y)) {

									for(k = 0; k<userFolders.size(); k++) {

										folderModels = userFolders.get(k).getModels();
										for(int x = 0; x < folderModels.size(); x++) {
											if(searchFilter.equalsIgnoreCase("1")) {
												tempModelName = folderModels.get(x).getmName().split("-");
												if(folderModels.get(x).getmName().equalsIgnoreCase(searchedElement)) {
													if(tempModelName.length > 1) {
														if(!tempModelName[1].equalsIgnoreCase("version")) {
															folderPathString = "";
															modelPath.add(userFolders.get(k).getfName());
															for(int s = 0; s<folderPath.size(); s++) {
																folderPathString = folderPathString.concat(folderPath.get(s) + "/");
															}
															folderPathString = folderPathString.concat(userFolders.get(k).getfName());
															modelPathList.add(folderPathString);
															modelPathString = "";
															folderPathString = "";
															searchedModels.add(folderModels.get(x));
														}
													}
													else {
														folderPathString = "";
														modelPath.add(userFolders.get(k).getfName());
														for(int s = 0; s<folderPath.size(); s++) {
															folderPathString = folderPathString.concat(folderPath.get(s) + "/");
														}
														folderPathString = folderPathString.concat(userFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														modelPathString = "";
														folderPathString = "";
														searchedModels.add(folderModels.get(x));
													}
													System.out.println("model added!");
												}
											}
											else if(searchFilter.equalsIgnoreCase("3")) {
												tempModelName = folderModels.get(x).getmName().split("-");
												if(folderModels.get(x).getCreationDate().equalsIgnoreCase(searchedElement)) {
													if(tempModelName.length > 1) {
														if(!tempModelName[1].equalsIgnoreCase("version")) {
															folderPathString = "";
															modelPath.add(userFolders.get(k).getfName());
															for(int s = 0; s<folderPath.size(); s++) {
																folderPathString = folderPathString.concat(folderPath.get(s) + "/");
															}
															folderPathString = folderPathString.concat(userFolders.get(k).getfName());
															modelPathList.add(folderPathString);
															modelPathString = "";
															folderPathString = "";
															searchedModels.add(folderModels.get(x));
														}
													}
													else {
														folderPathString = "";
														modelPath.add(userFolders.get(k).getfName());
														for(int s = 0; s<folderPath.size(); s++) {
															folderPathString = folderPathString.concat(folderPath.get(s) + "/");
														}
														folderPathString = folderPathString.concat(userFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														modelPathString = "";
														folderPathString = "";
														searchedModels.add(folderModels.get(x));
													}
													System.out.println("model added!");
												}
											}
										}
										System.out.println("foldername2: " + userFolders.get(k).getfName());
										if(searchFilter.equalsIgnoreCase("1")) {
											if(userFolders.get(k).getfName().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(userFolders.get(k));
												folderPathString = "";
												System.out.println("folder added!");
												for(int s = 0; s<folderPath.size(); s++) {
													if(s < folderPath.size()-1)
														folderPathString = folderPathString.concat(folderPath.get(s) + "/");
													else
														folderPathString = folderPathString.concat(folderPath.get(s));
												}
												folderPathList.add(folderPathString);
												folderPathString = "";
												System.out.println("folder path: " + folderPath);
											}
										}
										else if(searchFilter.equalsIgnoreCase("3")) {
											if(userFolders.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(userFolders.get(k));
												folderPathString = "";
												System.out.println("folder added!");
												for(int s = 0; s<folderPath.size(); s++) {
													if(s < folderPath.size()-1)
														folderPathString = folderPathString.concat(folderPath.get(s) + "/");
													else
														folderPathString = folderPathString.concat(folderPath.get(s));
												}
												folderPathList.add(folderPathString);
												folderPathString = "";
												System.out.println("folder path: " + folderPath);
											}
										}
									}
									checked.set(y, true);
								}
								j++;
							}
							for(k = 0; k<checked.size();k++) {
								System.out.println("checkedList: " + checked.get(k));
							}
						}
						System.out.println(searchedFolders.size());
						//y--;
						//userFolders = folderLists.get(y).get(listCounter.get(y)).getFolders();
						System.out.println("checpoint5");
						System.out.println("Y: " + y);
						System.out.println("folderList: " + folderLists);
						System.out.println("folderPath: " + folderPath);
						System.out.println("listcounter: " + listCounter);
						System.out.println(listCounter.get(y));
						System.out.println(folderLists.get(y).size() - 1);



						while(listCounter.get(y) == folderLists.get(y).size() - 1 && y!=0) {


							for(j=0;j<userFolders.size();j++) {
								System.out.println("foldernames: " + userFolders.get(j).getfName());
							}

							listCounter.remove(y);
							folderLists.remove(y);
							checked.remove(y);
							folderPath.remove(y-1);
							y--;
						}
						System.out.println("checpoint6");
						System.out.println("Y: " + y);
						System.out.println("folderList: " + folderLists);
						System.out.println("folderPath: " + folderPath);
						System.out.println("listcounter: " + listCounter);
						userFolders = folderLists.get(y);
						for(j = 0; j<folderLists.size();j++) {
							System.out.println("foldersizelist1: " + folderLists.get(j).size());
						}

						System.out.println(userFolders.get(0).getfName());


						for(j = 0; j<listCounter.size();j++) {
							System.out.println("listcounterlist: " + listCounter.get(j));
						}


						if(y == 0 && (listCounter.get(y) < folderLists.get(y).size() - 1)){

							listCounter.set(y, listCounter.get(y)+1);

							if(listCounter.get(0) != folderLists.get(0).size()) {
								j = listCounter.get(y);
								System.out.println("currentUserFolder: " + userFolders.get(j).getfName());
								while(userFolders.get(j).getFolders().isEmpty() && j < userFolders.size() - 1) {
									j++;
									listCounter.set(y, j);

								}
								if(!userFolders.get(j).getFolders().isEmpty()) {


									folderPath.add(userFolders.get(j).getfName());
									modelPath.add(userFolders.get(j).getfName());
									folderLists.add(userFolders.get(j).getFolders());
									userFolders = userFolders.get(j).getFolders();

									System.out.println("not null, got next folder list!");
									listCounter.add(0);
									checked.add(false);
								}

								else {
									listCounter.set(y, listCounter.get(y)+1);
								}

								for(j = 0; j<listCounter.size();j++) {
									System.out.println("listcounter: " + listCounter.get(j));
								}
								y++;
								System.out.println("previous folderlist size: " + folderLists.get(y-1).size() );

							}
						}
						else if(y == 0 && (listCounter.get(y) == folderLists.get(y).size() - 1)) {
							y++;
							listCounter.set(0, folderLists.get(0).size()); 
						}

						for(j = 0; j<folderLists.size();j++) {
							System.out.println("foldersizelist2: " + folderLists.get(j).size());
						}


					}
					System.out.println("Folders found are: " + searchedFolders.size());
					System.out.println("Models found are: " + searchedModels.size());
				}
			}
		}
		for(j = 0; j<folderPathList.size();j++) {
			System.out.println("folder path: " + folderPathList.get(j));
		}
		for(j = 0; j<modelPathList.size();j++) {
			System.out.println("model path: " + modelPathList.get(j));
		}
		search.setFolders(searchedFolders);
		search.setModels(searchedModels);
		search.setFolderPaths(folderPathList);
		search.setModelPaths(modelPathList);
		return search;
	}




	@PostMapping("/postFile/{folderPath:.+}+{username:.+}")
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable String folderPath, @PathVariable String username) {
		String message = "";
		folderPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + username + "&" + folderPath;
		folderPath = folderPath.replace("&", "\\");
		try {
			storageService.store(file, folderPath);
			files.add(file.getOriginalFilename());

			message = "You successfully uploaded " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.OK).body(message);
		} catch (Exception e) {
			message = "FAIL to upload " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
		}
	}

	@GetMapping("/getallfiles")
	public ResponseEntity<List<String>> getListFiles(Model model) {
		List<String> fileNames = files
				.stream().map(fileName -> MvcUriComponentsBuilder
						.fromMethodName(UserController.class, "getFile", fileName).build().toString())
				.collect(Collectors.toList());

		return ResponseEntity.ok().body(fileNames);
	}

	@GetMapping("/exportModelService/{fileName:.+}+{filePath:.+}+{username:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, @PathVariable String filePath, @PathVariable String username, HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource
		fileName = fileName.concat(".bpmn");
		Resource resource = storageService.loadFileAsResource(fileName, filePath, username);

		System.out.println(resource);
		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.print("ERROR");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}


	@GetMapping("/exportModelCollectionService")
	public ResponseEntity<Resource> downloadCollection( HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource
		String filePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\";
		String fileName = "exportedModels.zip";
		Resource resource = storageService.loadAsResource(filePath, fileName);
		System.out.println(resource);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.print("ERROR");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}



		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}


	@PostMapping("/deleteCollection")
	public void deleteCollection() {

		System.out.println("DELETE COLLECTION");

		File file = new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\exportedModels.zip");
		file.delete();

		return;
	}

	@GetMapping("/openModel/{fileName:.+}+{filePath:.+}+{username:.+}")
	public ResponseEntity<Resource> openModel(@PathVariable String fileName, @PathVariable String filePath, @PathVariable String username, HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource

		System.out.println("open Model!");


		fileName = fileName.concat(".bpmn");

		Resource resource = storageService.loadFileAsResource(fileName, filePath, username);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.print("ERROR");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}


	@GetMapping("/shareModelService/{fileName:.+}+{filePath:.+}+{username:.+}")
	public ResponseEntity<Resource> shareModelService(@PathVariable String fileName, @PathVariable String filePath, @PathVariable String username, HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource
		fileName = fileName.concat(".bpmn");
		Resource resource = storageService.loadFileAsResource(fileName, filePath, username);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.print("ERROR");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}


	@PostMapping("/getShareCode/{fileName:.+}+{filePath:.+}")
	public ShareCode getShareCode(@PathVariable String fileName, @PathVariable String filePath, @RequestBody User user){
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String dbCurrentLocation = filePath;
		boolean found = false;
		String[] splittedFolders = dbCurrentLocation.split("&");


		ShareCode shareCode = new ShareCode();

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(fileName)) {
										shareCode.setShareCode(folderModels.get(y).getShareCode());
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}

			}
		}
		return shareCode;
	}


	@GetMapping("/downloadSharedModel/{shareCode:.+}")
	public ModelAndView downloadSharedModel(@PathVariable String shareCode){

		System.out.println("Download shared model!");
		User user = new User();
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<Folder> searchedFolders = new ArrayList<Folder>();
		List<BpmnModel> searchedModels = new ArrayList<BpmnModel>();
		List<Integer> listCounter = new ArrayList<Integer>();
		List<Boolean> checked = new ArrayList<Boolean>();
		Search search = new Search();
		List<String> folderPath = new ArrayList<String>();
		List<String> folderPathList = new ArrayList<String>();
		String folderPathString = "";
		String modelPathString = "";
		List<String> modelPath = new ArrayList<String>();
		List<String> modelPathList = new ArrayList<String>();
		//List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		//String[] splittedFolders = folderPath.split("&");
		int i,j,k,y = 0;
		for(i = 0; i<users.size(); i++) {

			y=0;

			for(j=0;j<userFolders.size();j++) {
				userFolders.remove(j);
			}
			for(j=0;j<folderPath.size();j++) {
				folderPath.remove(j);
			}
			for(j=0;j<modelPath.size();j++) {
				modelPath.remove(j);
			}
			for(j=0;j<listCounter.size();j++) {
				listCounter.remove(j);
			}
			for(j=0;j<checked.size();j++) {
				checked.remove(j);
			}
			for(j=0;j<folderLists.size();j++) {
				folderLists.remove(j);
			}

			System.out.println("checkpoint1");
			user = users.get(i);
			userFolders = user.getFolders();
			folderLists.add(user.getFolders());
			for(j = 0; j<userFolders.size(); j++) {
				folderModels = userFolders.get(j).getModels();
				for(k = 0; k<folderModels.size();k++) {
					if(folderModels.get(k).getShareCode().equalsIgnoreCase(shareCode)) {
						System.out.println("checkpoint2");
						modelPath.add(userFolders.get(j).getfName());
						folderPathString = "";
						System.out.println("checkpoint3");
						for(int s = 0; s<folderPath.size(); s++) {
							folderPathString = folderPathString.concat(folderPath.get(s) + "&");
						}
						folderPathString = folderPathString.concat(userFolders.get(j).getfName());
						modelPathList.add(folderPathString);
						System.out.println("checkpoint4");
						System.out.println("Arrivato0");
						return new ModelAndView("redirect:/api/exportModelService/" + folderModels.get(k).getmName() + "+" + folderPathString + "+" + user.getUsername());	
					}
				}

			}
			System.out.println("checkpoint22");
			j=0;
			System.out.println(userFolders.get(j).getfName());
			while(userFolders.get(j).getFolders().isEmpty() && j<userFolders.size()-1) {
				j++;
				System.out.println(j);
			}
			System.out.println("checkpoint23");
			if(!userFolders.get(j).getFolders().isEmpty()) {
				System.out.println("checkpoint3");
				checked.add(true);
				listCounter.add(j);
				y++;
				modelPath.add(userFolders.get(j).getfName());
				folderPath.add(userFolders.get(j).getfName());
				folderLists.add(userFolders.get(j).getFolders());
				userFolders = userFolders.get(j).getFolders();

				System.out.println("not null, got next folder list!");
				listCounter.add(0);
				checked.add(false);
				System.out.println("checkpoint4");
			}
			for(j = 0; j<listCounter.size();j++) {
				System.out.println("listcounter: " + listCounter.get(j));
			}

			System.out.println("checkpoint24");
			if(y!=0){
				//System.out.println("previous folderlist size: " + folderLists.get(y-1).size() );
				while(listCounter.get(y-1) < folderLists.get(y-1).size() - 1 || listCounter.get(0) < folderLists.get(0).size()) {
					System.out.println("start the while in which i loop until the previous folder is not complete");
					if(checked.get(y)) {
						listCounter.set(y,listCounter.get(y)+1);
					}
					j = listCounter.get(y);
					while(j<userFolders.size()) {
						System.out.println("curret y: " + y);
						if(!userFolders.get(j).getFolders().isEmpty()) {

							if(!checked.get(y)) {

								for(k = 0; k<userFolders.size(); k++) {
									folderModels = userFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(folderModels.get(x).getShareCode().equalsIgnoreCase(shareCode)) {
											modelPath.add(userFolders.get(k).getfName());
											folderPathString = "";
											for(int s = 0; s<folderPath.size(); s++) {
												folderPathString = folderPathString.concat(folderPath.get(s) + "&");
											}
											folderPathString = folderPathString.concat(userFolders.get(k).getfName());
											modelPathList.add(folderPathString);	
											System.out.println("Arrivato1");
											System.out.println("model added!");
											modelPathString = "";


											return new ModelAndView("redirect:/api/exportModelService/" + folderModels.get(x).getmName() + "+" + folderPathString + "+" + user.getUsername());	
										}
									}
									System.out.println("foldername1: " + userFolders.get(k).getfName());

								}
								checked.set(y, true);
								System.out.println("checkpoint7");
							}

							checked.add(false);
							listCounter.set(y,j);
							folderPath.add(userFolders.get(j).getfName());
							modelPath.add(userFolders.get(j).getfName());
							folderLists.add(userFolders.get(j).getFolders());
							userFolders = userFolders.get(j).getFolders();
							listCounter.add(0);
							y++;
							//System.out.println("Y: " + y);
							for(k = 0; k<listCounter.size();k++) {
								System.out.println("listcounter: " + listCounter.get(k));
							}
							j = 0;
						}

						else {
							if(!checked.get(y)) {

								for(k = 0; k<userFolders.size(); k++) {

									folderModels = userFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(folderModels.get(x).getShareCode().equalsIgnoreCase(shareCode)) {
											folderPathString = "";
											modelPath.add(userFolders.get(k).getfName());
											for(int s = 0; s<folderPath.size(); s++) {
												folderPathString = folderPathString.concat(folderPath.get(s) + "&");
											}
											folderPathString = folderPathString.concat(userFolders.get(k).getfName());
											System.out.println("Arrivato2");
											modelPathList.add(folderPathString);
											searchedModels.add(folderModels.get(x));
											modelPathString = "";

											return new ModelAndView("redirect:/api/exportModelService/" + folderModels.get(x).getmName() + "+" + folderPathString + "+" + user.getUsername());	

										}
									}
									System.out.println("foldername2: " + userFolders.get(k).getfName());

								}
								checked.set(y, true);
							}
							j++;
						}
						for(k = 0; k<checked.size();k++) {
							System.out.println("checkedList: " + checked.get(k));
						}
					}
					System.out.println(searchedFolders.size());
					//y--;
					//userFolders = folderLists.get(y).get(listCounter.get(y)).getFolders();

					System.out.println("checkpoint8");

					while(listCounter.get(y) == folderLists.get(y).size() - 1 && y!=0) {


						for(j=0;j<userFolders.size();j++) {
							System.out.println("foldernames: " + userFolders.get(j).getfName());
						}

						listCounter.remove(y);
						folderLists.remove(y);
						checked.remove(y);
						folderPath.remove(y-1);
						y--;
					}
					userFolders = folderLists.get(y);
					for(j = 0; j<folderLists.size();j++) {
						System.out.println("foldersizelist1: " + folderLists.get(j).size());
					}

					System.out.println(userFolders.get(0).getfName());

					for(j = 0; j<listCounter.size();j++) {
						System.out.println("listcounterlist: " + listCounter.get(j));
					}


					if(y == 0 && (listCounter.get(y) < folderLists.get(y).size() - 1)){

						listCounter.set(y, listCounter.get(y)+1);

						if(listCounter.get(0) != folderLists.get(0).size()) {
							j = listCounter.get(y);
							System.out.println("currentUserFolder: " + userFolders.get(j).getfName());
							while(userFolders.get(j).getFolders().isEmpty() && j < userFolders.size() - 1) {
								j++;
								listCounter.set(y, j);

							}
							if(!userFolders.get(j).getFolders().isEmpty()) {


								folderPath.add(userFolders.get(j).getfName());
								modelPath.add(userFolders.get(j).getfName());
								folderLists.add(userFolders.get(j).getFolders());
								userFolders = userFolders.get(j).getFolders();

								System.out.println("not null, got next folder list!");
								listCounter.add(0);
								checked.add(false);
							}

							else {
								listCounter.set(y, listCounter.get(y)+1);
							}

							for(j = 0; j<listCounter.size();j++) {
								System.out.println("listcounter: " + listCounter.get(j));
							}
							y++;
							System.out.println("previous folderlist size: " + folderLists.get(y-1).size() );

						}
					}
					else if(y == 0 && (listCounter.get(y) == folderLists.get(y).size() - 1)) {
						y++;
						listCounter.set(0, folderLists.get(0).size()); 
					}

					for(j = 0; j<folderLists.size();j++) {
						System.out.println("foldersizelist2: " + folderLists.get(j).size());
					}


				}
				System.out.println("Folders found are: " + searchedFolders.size());
				System.out.println("Models found are: " + searchedModels.size());
			}

		}
		for(j = 0; j<folderPathList.size();j++) {
			System.out.println("folder path: " + folderPathList.get(j));
		}
		for(j = 0; j<modelPathList.size();j++) {
			System.out.println("model path: " + modelPathList.get(j));
		}




		return new ModelAndView("redirect:/api/exportModelService/filenotfound");	
	}


	@PostMapping("/publishModelService/{fileName:.+}+{filePath:.+}")
	public int publishModelService(@PathVariable String fileName, @PathVariable String filePath, @RequestBody User user) throws IOException{

		BigInteger modelsId = BigInteger.valueOf(0);
		System.out.println("publish model!");
		List<BpmnModel> modelsToShare = new ArrayList<BpmnModel>();
		List<PublicFolder> publicFolders = publicRepository.findAll();
		String[] splittedFolders = filePath.split("&");
		String[] modelSplitted;
		String newPath = "Shared Files";
		List<String> modelToShareNames = new ArrayList<String>();
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		String modelNameTxt;
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		BigInteger modelId = BigInteger.valueOf(0);
		filePath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + filePath;
		newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&Shared Files&";
		// Move the model locally
		boolean success = false;
		if (newPath.equals(""))
			return 0;
		modelNameTxt = fileName.concat(".bpmn");
		//newPath = newPath.concat(modelNameTxt);
		newPath = newPath.replace("&", "\\");
		//filePath = filePath.replace("&", "\\").concat("\\" + modelNameTxt);
		filePath = filePath.replace("&", "\\");

		Path source, dest; 
		
		for (int i = 0; i < publicFolders.size(); i++) {
			if(publicFolders.get(i).getfName().equalsIgnoreCase("Shared Files")) {
				modelsId = publicFolders.get(i).getModelsId();
				System.out.println(modelsId);
			}
		}
		
		
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();

				for (int j = 0; j < splittedFolders.length; j++) {
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j])) {
							folderModels = userFolders.get(k).getModels();
							for (int y = 0; y < folderModels.size(); y++) {
								modelSplitted = folderModels.get(y).getmName().split("-");
								if(modelSplitted.length>1) {
									if(modelSplitted[3].equalsIgnoreCase(fileName)) {
										modelsId = modelsId.add(BigInteger.ONE);
										folderModels.get(y).setmId(modelsId);
										modelsToShare.add(folderModels.get(y));
										modelToShareNames.add(folderModels.get(y).getmName());
										
									}
								}
								else {
									if (folderModels.get(y).getmName().equals(fileName)) {

										modelsId = modelsId.add(BigInteger.ONE);
										folderModels.get(y).setmId(modelsId);
										modelsToShare.add(folderModels.get(y));
										modelToShareNames.add(folderModels.get(y).getmName());
										folderModels.remove(y);
										userFolders.get(k).setModels(folderModels);
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
			}
		}


		for (int i = 0; i < publicFolders.size(); i++) {
			System.out.println(publicFolders.get(i).getfName());
			if(publicFolders.get(i).getfName().equalsIgnoreCase("Shared Files")) {
				List<BpmnModel> models = new ArrayList<BpmnModel>();
				models = publicFolders.get(i).getModels();
				for(int j = 0; j<models.size();j++) {
					if(models.get(j).getmName().equalsIgnoreCase(fileName) && models.get(j).getAuthor().equalsIgnoreCase(user.getUsername())) {
						return 1;
					} else if(models.get(j).getmName().equalsIgnoreCase(fileName))
						return 2;

				}
				for(int j = 0; j<modelsToShare.size();j++) {
					models.add(modelsToShare.get(j));
				}
				publicFolders.get(i).setModelsId(modelsId);
				publicFolders.get(i).setModels(models);
				
				publicRepository.save(publicFolders.get(i));
			}
		}

		for(int i = 0; i<modelToShareNames.size(); i++) {
			source = Paths.get(filePath.concat("\\" + modelToShareNames.get(i) + ".bpmn"));
			dest = Paths.get(newPath.concat(modelToShareNames.get(i) + ".bpmn"));
			Files.copy(source, dest, LinkOption.NOFOLLOW_LINKS);
		}

		return 0;
	}





	@PostMapping("/exportModelCollection/{folderPath:.+}+{username:.+}")
	public boolean exportModelCollection(@PathVariable String folderPath, @PathVariable String username, @RequestBody String[] modelList) throws IOException{

		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<BpmnModel> model = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsFiltered = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsToExport = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");
		String[] splittedModel;
		boolean found = false;
		String modelName;
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(username)) {
				userFolders = users.get(i).getFolders();
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							if(j == splittedFolders.length - 1) {
								model = userFolders.get(k).getModels();
								for(int y = 0; y<model.size(); y++) {
									for(int z = 0; z<modelList.length; z++) {
										if(model.get(y).getmName().equalsIgnoreCase(modelList[z])) {
											modelsToExport.add(model.get(y));
											System.out.println(model.get(y).getmName() + " Added!");
										}
									}


								}
							}
							userFolders = userFolders.get(k).getFolders();
						}
					}
				}
			}
		}

		folderPath = folderPath.replace("&", "\\");

		List<File> files = new ArrayList<File>();
		for(int i = 0; i<modelList.length; i++) {
			String path = "C:\\users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + username + "\\" + folderPath + "\\" + modelList[i] + ".bpmn";
			File file = new File(path);
			files.add(file);
		}

		File zipfile = new File("C:\\users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\exportedModels.zip");
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];
		try {
			// create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
			// compress the files
			for(int i=0; i<files.size(); i++) {
				FileInputStream in = new FileInputStream(files.get(i).getCanonicalFile());
				// add ZIP entry to output stream
				out.putNextEntry(new ZipEntry(files.get(i).getName()));
				// transfer bytes from the file to the ZIP file
				int len;
				while((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// complete the entry
				out.closeEntry();
				in.close();
			}
			// complete the ZIP file
			out.close();
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}

		return true;	

	}



	@PostMapping("/createModel/{folderPath:.+}+{fileName:.+}")
	public boolean createModel(@PathVariable String folderPath, @PathVariable String fileName, @RequestBody User user) throws IOException{

		System.out.println(folderPath);
		System.out.println(fileName);

		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsFiltered = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsToExport = new ArrayList<BpmnModel>();
		String source = "C:\\Users\\loren\\git\\SPM-2018-FSB\\emptyModel.bpmn";
		String dest = "C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + user.getUsername() + "\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";
		String[] splittedFolders = folderPath.split("&");
		String[] splittedModel;
		BigInteger modelId = BigInteger.valueOf(0);
		boolean found = false;
		String currentDate = getCurrentDate();
		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());
		String randomString = gen.nextString();

		System.out.println("checkpoin1");
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				modelId = users.get(i).getModelsId();
				System.out.println("checkpoin2");
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						System.out.println("checkpoin3");
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							System.out.println("checkpoin4");
							System.out.println(userFolders.get(k).getfName());
							System.out.println(splittedFolders[j]);
							if(j == splittedFolders.length - 1) {
								models = userFolders.get(k).getModels();
								for(int y = 0; y<models.size(); y++) {
									if(models.get(y).getmName().equals(fileName)) {
										return false;
									}
								}
								System.out.println("checkpoin5");
								BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
								model.setShareCode(randomString);
								model.setVersionNumber(1);
								model.setVersionDescription("First version");
								model.setModelNumber(user.getModelsId().intValue());
								model.setCreationDate(currentDate);
								model.setAuthor(user.getUsername());
								models.add(model);
								userFolders.get(k).setModels(models);

							}
							folderLists.add(user.getFolders());
							userFolders = userFolders.get(k).getFolders();
							System.out.println("checkpoin6");
						}
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setModelsId(user.getModelsId().add(BigInteger.ONE));
				userRepository.save(user);
				System.out.println("checkpoin7");
			}

		}
		for (int i = 0; i < users.size(); i++) {
			userRepository.save(users.get(i));
		}
		System.out.println("checkpoin8");

		Path sourcePath = Paths.get(source);
		Path destPath = Paths.get(dest);
		Files.copy(sourcePath, destPath, LinkOption.NOFOLLOW_LINKS);

		return true;
	}


	@PostMapping("/saveModel/{username:.+}+{folderPath:.+}+{fileName:.+}")
	public void saveModel(@PathVariable String username, @PathVariable String folderPath, @PathVariable String fileName, @RequestBody Xmlfile xml) throws IOException{
		System.out.println("Save Model!");
		System.out.println("folderPath: " + folderPath);
		System.out.println("fileName: " + fileName);
		System.out.println("username: " + username);

		Xmlfile xmlToSave = new Xmlfile();
		xmlToSave = xml;
		System.out.println("xml: " + xml.getXml());

		String savePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + username + "\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";

		try (PrintWriter out = new PrintWriter(savePath)) {
			out.println(xml.getXml());
		}

		/*List<User> users = userRepository.findAll();
		User user = new User();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsFiltered = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsToExport = new ArrayList<BpmnModel>();
		String source = "C:\\Users\\loren\\git\\SPM-2018-FSB\\emptyModel.bpmn";
		String dest = "C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + user.getUsername() + "\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";
		String[] splittedFolders = folderPath.split("&");
		String[] splittedModel;
		BigInteger modelId = BigInteger.valueOf(0);
		boolean found = false;

		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(username)) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				modelId = users.get(i).getModelsId();
				System.out.println("checkpoin2");
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						System.out.println("checkpoin3");
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							System.out.println("checkpoin4");
							System.out.println(userFolders.get(k).getfName());
							System.out.println(splittedFolders[j]);
							if(j == splittedFolders.length - 1) {
								models = userFolders.get(k).getModels();
								for(int y = 0; y<models.size(); y++) {
									if(models.get(y).getmName().equalsIgnoreCase(fileName)) {
										try (PrintWriter out = new PrintWriter("C:\\Users\\loren\\Desktop\\emptyModel.bpmn")) {
										    out.println(xml);
										}
									}
								}


							}
							folderLists.add(user.getFolders());
							userFolders = userFolders.get(k).getFolders();
							System.out.println("checkpoin6");
						}
					}
				}
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setModelsId(user.getModelsId().add(BigInteger.ONE));
				userRepository.save(user);
				System.out.println("checkpoin7");
			}

		}
		for (int i = 0; i < users.size(); i++) {
			userRepository.save(users.get(i));
		}*/
		

	}

	@PostMapping("/saveModelAsNewVersion/{username:.+}+{folderPath:.+}+{fileName:.+}+{versionDescription:.+}")
	public void saveModelAsNewVersion(@PathVariable String username, @PathVariable String folderPath, @PathVariable String fileName, @PathVariable String versionDescription, @RequestBody Xmlfile xml) throws IOException{
	

		
		System.out.println("versionDescription: " + versionDescription);
		List<User> users = userRepository.findAll();
		List<Folder> userFolders = new ArrayList<Folder>();
		List<List<Folder>> folderLists = new ArrayList<List<Folder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		User user = new User();
		String[] splittedFolders = folderPath.split("&");
		int getPreviousVersion = 0, getModelNumber = 0;
		boolean found = false;
		boolean versionAdded = false;
		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());

		String randomString = gen.nextString();


		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(username)) {
				user = users.get(i);
				userFolders = users.get(i).getFolders();
				folderLists.add(user.getFolders());
				System.out.println("checkpoint1");
				for (int j = 0; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < userFolders.size(); k++) {
						if (userFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							System.out.println("checkpoint2");
							found = true;
							if(j == splittedFolders.length - 1) {
								folderLists.add(userFolders.get(k).getFolders());
								folderModels = userFolders.get(k).getModels();
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(fileName) && !versionAdded) {
										versionAdded = true;
										System.out.println("checkpoint3");
										BpmnModel model = new BpmnModel(user.getModelsId().add(BigInteger.ONE), fileName);
										getPreviousVersion = folderModels.get(y).getVersionNumber();
										getModelNumber = folderModels.get(y).getModelNumber();
										model.setShareCode(randomString);
										model.setModelNumber(folderModels.get(y).getModelNumber());
										model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
										model.setVersionDescription(versionDescription);
										model.setCreationDate(getCurrentDate());
										model.setAuthor(user.getUsername());
										folderModels.add(model);
										folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + fileName);
										userFolders.get(k).setModels(folderModels);
										System.out.println("checkpoint4");
									}
								}
							}
							userFolders = userFolders.get(k).getFolders();
						}

					}
				}
				System.out.println("checkpoint5");
				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(userFolders);
				}
				user.setFolders(folderLists.get(0));
				user.setModelsId(user.getModelsId().add(BigInteger.ONE));
				userRepository.save(user);
				System.out.println("checkpoint6");
			}
		}

		System.out.println("checkpoint7");


		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_"
				+ user.getUsername() + "&" + folderPath + "&" + fileName + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&UsersRepository&main_" + user.getUsername()
		+ "&" + folderPath + "&m" + getModelNumber + "-version-" + getPreviousVersion + "-" + fileName + ".bpmn";
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		Path source = Paths.get(newPath);
		Path dest = Paths.get(currentPath);
		Files.copy(source, dest, LinkOption.NOFOLLOW_LINKS);

		
		Xmlfile xmlToSave = new Xmlfile();
		xmlToSave = xml;
		System.out.println("xml: " + xml.getXml());

		String savePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\UsersRepository\\main_" + username + "\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";

		try (PrintWriter out = new PrintWriter(savePath)) {
			out.println(xml.getXml());
		}
	}
	
	@PostMapping("/verifyValidity/{username:.+}+{filePath:.+}+{fileName:.+}")
	public ResponseEntity<Resource> verifyValidity(@PathVariable String username, @PathVariable String filePath, @PathVariable String fileName,  HttpServletRequest request) throws IOException{
		System.out.println("VERIFY VALIDITY");
		fileName = fileName.concat(".bpmn");
		Resource resource = storageService.loadFileAsResource(fileName, filePath, username);
		
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			System.out.print("ERROR");
		}

		// Fallback to the default content type if type could not be determined
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);	
		}

	public String getCurrentDate(){
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String currentDate = localDate.format(formatter);
		return currentDate;
	}
}