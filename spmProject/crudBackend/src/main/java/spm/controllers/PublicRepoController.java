package spm.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import spm.entities.BpmnModel;
import spm.entities.Folder;
import spm.entities.PublicFolder;
import spm.entities.PublicSearch;
import spm.entities.Search;
import spm.entities.ShareCode;
import spm.entities.User;
import spm.entities.Version;
import spm.entities.Xmlfile;
import spm.repositories.PublicMongoRepository;
import spm.service.StorageService;
import spm.utilities.RandomGenerator;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class PublicRepoController {

	@Autowired
	private PublicMongoRepository publicRepository;

	@Autowired
	StorageService storageService;

	List<String> files = new ArrayList<String>();

	@PostMapping("/createPublicFolder/{createdName}+{username:.+}")
	public boolean createPublicFolder(@PathVariable String createdName, @PathVariable String username) {
		BigInteger folderId = BigInteger.valueOf(0);
		BigInteger modelsId = BigInteger.valueOf(0);
		String currentDate = getCurrentDate();
		List<PublicFolder> folders = publicRepository.findAll();
		for (int i = 0; i < folders.size(); i++) {
			if (folderId.compareTo(folders.get(i).getFoldersId()) == -1)
				folderId = folders.get(i).getFoldersId();
			if (modelsId.compareTo(folders.get(i).getModelsId()) == -1)
				modelsId = folders.get(i).getModelsId();
			if (folders.get(i).getfName().equalsIgnoreCase(createdName))
				return false;
		}
		if (createdName.equalsIgnoreCase(""))
			return false;
		PublicFolder folder = new PublicFolder(folderId.add(BigInteger.ONE), createdName);
		List<PublicFolder> emptyFolders = new ArrayList<PublicFolder>();
		folder.setFolders(emptyFolders);
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		folder.setCreationDate(currentDate);
		folder.setModels(models);
		folder.setFoldersId(folderId.add(BigInteger.ONE));
		folder.setAuthor(username);
		folder.setModelsId(modelsId);
		publicRepository.save(folder);
		File f = new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + createdName);
		f.mkdir();
		return true;
	}

	@PostMapping("/createPublicInnerFolder/{currentFolder}+{createdName}+{username:.+}")
	public boolean createPublicInnerFolder(@PathVariable String currentFolder, @PathVariable String createdName, @PathVariable String username) {
		boolean success = true;
		if (createdName.equals(""))
			return false;
		PublicFolder folder = new PublicFolder();
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<PublicFolder> folderInnerFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		String[] splittedFolders = currentFolder.split("&");
		BigInteger folderId = BigInteger.valueOf(0);
		String currentDate = getCurrentDate();
		BigInteger currentModelId = BigInteger.valueOf(0);

		boolean found = false;
		System.out.println("checkpoint-2");
		for (int i = 0; i < folders.size(); i++) {
			if (folderId.compareTo(folders.get(i).getFoldersId()) == -1)
				folderId = folders.get(i).getFoldersId();
		}
		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				folder = folders.get(i);
				currentModelId = folders.get(i).getModelsId();

			}
		}
		System.out.println("checkpoint-1");
		System.out.println(currentModelId);
		System.out.println(splittedFolders.length);
		folderFolders = folder.getFolders();
		folderLists.add(folder.getFolders());
		if (splittedFolders.length == 1) {
			for (int i = 0; i < folderFolders.size(); i++) {
				if (folderFolders.get(i).getfName().equalsIgnoreCase(createdName))
					return false;
			}
			System.out.println("checkpoint0");
			PublicFolder innerFolder = new PublicFolder(folderId.add(BigInteger.ONE), createdName);
			List<PublicFolder> emptyfolders = new ArrayList<PublicFolder>();
			List<BpmnModel> models = new ArrayList<BpmnModel>();
			innerFolder.setFolders(emptyfolders);
			innerFolder.setModels(models);
			innerFolder.setCreationDate(currentDate);
			innerFolder.setAuthor(username);
			folderFolders.add(innerFolder);
			folder.setFolders(folderFolders);
			folder.setFoldersId(folderId.add(BigInteger.ONE));
			folder.setModelsId(currentModelId);
			System.out.println("checkpoint1");
			publicRepository.save(folder);
		} else {
			System.out.println("checkpoint1");
			for (int j = 1; j < splittedFolders.length; j++) {
				found = false;
				for (int k = 0; k < folderFolders.size(); k++) {
					if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
						found = true;
						folderLists.add(folderFolders.get(k).getFolders());
						folderFolders = folderFolders.get(k).getFolders();

						if (j == splittedFolders.length - 1) {
							for (int i = 0; i < folderFolders.size(); i++) {
								if (folderFolders.get(i).getfName().equalsIgnoreCase(createdName))
									return false;
							}
							System.out.println("checkpoint2");
							PublicFolder innerFolder = new PublicFolder(folderId.add(BigInteger.ONE), createdName);
							List<PublicFolder> emptyfolders = new ArrayList<PublicFolder>();
							List<BpmnModel> models = new ArrayList<BpmnModel>();
							innerFolder.setFolders(emptyfolders);
							innerFolder.setModels(models);
							innerFolder.setCreationDate(currentDate);
							innerFolder.setAuthor(username);
							folderFolders.add(innerFolder);
							System.out.println("checkpoint3");
						}
					}
				}
			}
			for (int j = folderLists.size() - 1; j <= 0; j--) {
				folderLists.get(j).get(0).setFolders(folderFolders);
			}
			System.out.println("checkpoint4");
			folder.setFolders(folderLists.get(0));
			folder.setFoldersId(folderId.add(BigInteger.ONE));
			//folder.setModelsId(currentModelId);
			publicRepository.save(folder);
		}
		String mergedPath = String.join("\\", splittedFolders);
		File f = new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + mergedPath + "\\" + createdName);
		f.mkdir();
		return success;
	}

	@PostMapping("/renamePublicFolder/{currentName:.+}+{newName:.+}+{folderPath:.+}+{renameType}")
	public boolean renamePublicFolder(@PathVariable String currentName, @PathVariable String newName,
			@PathVariable String folderPath, @PathVariable String renameType) {

		String currentPath;
		String newPath;
		String previousPath = "";
		
		
		if (newName.equals(""))
			return false;

		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<PublicFolder> folderInnerFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		String[] splittedFolders = folderPath.split("&");
		boolean found = false;

		if(!renameType.equals("root")) {
			if (!folderPath.equals("root")) {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath + "&" + currentName;
				newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath + "&" + newName;
			} else {
				currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + currentName;
				newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + newName;
			}
		}
		else {
			for(int i = 0; i<splittedFolders.length - 1; i++) {
				previousPath = previousPath.concat(splittedFolders[i] + "&");
			}
			currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + previousPath + currentName;
			newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + previousPath + newName;
		}





		if(splittedFolders.length == 1) {
			if(renameType.equals("root")) {
				for(int i = 0; i<folders.size(); i++) {
					if(folders.get(i).getfName().equals(newName)) 
						return false;	
				}
				for(int i = 0; i<folders.size(); i++) {
					if(folders.get(i).getfName().equals(splittedFolders[0])) {
						folders.get(i).setfName(newName);
						publicRepository.save(folders);

						currentPath = currentPath.replace("&", "\\");
						newPath = newPath.replace("&", "\\");
						File model = new File(currentPath);
						model.renameTo(new File(newPath));

						return true;
					}
				}
			}
		}


		if (folderPath.equals("root")) {
			for(int i = 0; i<folders.size(); i++) {
				if (folders.get(i).getfName().equalsIgnoreCase(newName))
					return false;
			}
			for(int i = 0; i<folders.size(); i++) {

				if (folders.get(i).getfName().equalsIgnoreCase(currentName)) {
					folders.get(i).setfName(newName);
					publicRepository.save(folders);
					currentPath = currentPath.replace("&", "\\");
					newPath = newPath.replace("&", "\\");
					File model = new File(currentPath);
					model.renameTo(new File(newPath));
					return true;
				}
			}

		}

		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				found = false;
				folderFolders = folders.get(i).getFolders();
				folderLists.add(folders.get(i).getFolders());
				if (!folderPath.equals("root")) {

					if (splittedFolders.length == 1) {
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equalsIgnoreCase(newName)) {
								return false;
							}
						}
						for (int k = 0; k < folderFolders.size(); k++) {

							if (folderFolders.get(k).getfName().equalsIgnoreCase(currentName) && !found) {
								found = true;
								folderFolders.get(k).setfName(newName);
								folders.get(i).setFolders(folderFolders);
								publicRepository.save(folders);
								currentPath = currentPath.replace("&", "\\");
								newPath = newPath.replace("&", "\\");
								File model = new File(currentPath);
								model.renameTo(new File(newPath));
								return true;
							}
						}
					} else {
						for (int j = 1; j < splittedFolders.length; j++) {
							found = false;
							for (int k = 0; k < folderFolders.size(); k++) {

								if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
									found = true;
									if (renameType.equals("root")) {


										if (j == splittedFolders.length - 1) {
											for (int y = 0; y < folderFolders.size(); y++) {
												if (folderFolders.get(y).getfName().equalsIgnoreCase(newName)) {
													return false;
												}
											}
											for (int y = 0; y < folderFolders.size(); y++) {
												if (folderFolders.get(y).getfName().equalsIgnoreCase(currentName)) {
													folderFolders.get(y).setfName(newName);
												}
											}
										}
										folderLists.add(folderFolders.get(k).getFolders());
										folderFolders = folderFolders.get(k).getFolders();
									}
									else {
										folderLists.add(folderFolders.get(k).getFolders());
										folderFolders = folderFolders.get(k).getFolders();
										if (j == splittedFolders.length - 1) {
											for (int y = 0; y < folderFolders.size(); y++) {
												if (folderFolders.get(y).getfName().equalsIgnoreCase(newName)) {
													return false;
												}
											}
											for (int y = 0; y < folderFolders.size(); y++) {
												if (folderFolders.get(y).getfName().equalsIgnoreCase(currentName)) {
													folderFolders.get(y).setfName(newName);
												}
											}
										}

									}
								}
							}
						}
						for (int j = folderLists.size() - 1; j <= 0; j--) {
							folderLists.get(j).get(0).setFolders(folderFolders);
						}
						folders.get(i).setFolders(folderLists.get(0));
						publicRepository.save(folders);
						currentPath = currentPath.replace("&", "\\");
						newPath = newPath.replace("&", "\\");

						File model = new File(currentPath);
						model.renameTo(new File(newPath));

						return true;
					}
				} 
			}

		}
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		return true;
	}


	@GetMapping("/downloadPublicSharedModel/{shareCode:.+}")
	public ModelAndView downloadSharedModel(@PathVariable String shareCode){
		System.out.println("Download shared model!");
		System.out.println("shareCode: " + shareCode);

		int i,j,k,y = 0;
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<PublicFolder> searchedFolders = new ArrayList<PublicFolder>();
		List<BpmnModel> searchedModels = new ArrayList<BpmnModel>();
		List<Integer> listCounter = new ArrayList<Integer>();
		List<Boolean> checked = new ArrayList<Boolean>();
		PublicSearch search = new PublicSearch();
		List<String> folderPath = new ArrayList<String>();
		List<String> folderPathList = new ArrayList<String>();
		String folderPathString = "";
		String modelPathString = "";
		List<String> modelPath = new ArrayList<String>();
		List<String> modelPathList = new ArrayList<String>();
		String foldertoiterate = "";
		folderLists.add(folders);
		String checkerror = "";
		int checkerrorint = 0;
		for(i = 0; i<folders.size(); i++) {


			folderModels = folders.get(i).getModels();
			for(j = 0; j < folderModels.size(); j++) {
				if(folderModels.get(j).getShareCode().equalsIgnoreCase(shareCode)) {
					folderPathString = "";
					modelPath.add(folders.get(i).getfName());

					folderPathString = folderPathString.concat(folders.get(i).getfName());
					modelPathList.add(folderPathString);
					System.out.println("model added!2");
					return new ModelAndView("redirect:/api/exportPublicModelService/" + folderModels.get(j).getmName() + "+" + folderPathString);	
				}
			}
			j = 0;
			while(folders.get(j).getFolders().isEmpty() && j<folders.size()-1) {

				System.out.println("j: " + j);
				j++;
				checkerrorint++;
			}
			checkerror = folders.get(j).getfName() + " is not empty";

			if(!folders.get(j).getFolders().isEmpty()) {
				checked.add(true);
				listCounter.add(j);
				y++;
				modelPath.add(folders.get(j).getfName());
				folderPath.add(folders.get(j).getfName());
				folderLists.add(folders.get(j).getFolders());
				folderFolders = folders.get(j).getFolders();
				foldertoiterate = folders.get(j).getfName();

				System.out.println("not null, got next folder list! 1");
				listCounter.add(0);
				checked.add(false);
			}
			for(j = 0; j<listCounter.size();j++) {
				System.out.println("listcounter: " + listCounter.get(j));
			}


			if(y != 0) {
				while(listCounter.get(y-1) < folderLists.get(y-1).size() - 1 || listCounter.get(0) < folderLists.get(0).size()) {
					System.out.println("start the while in which i loop until the previous folder is not complete");

					if(checked.get(y))
						listCounter.set(y,listCounter.get(y)+1);
					j = listCounter.get(y);
					while(j<folderFolders.size()) {
						if(!folderFolders.get(j).getFolders().isEmpty()) {
							if(!checked.get(y)) {

								for(k = 0; k<folderFolders.size(); k++) {
									folderModels = folderFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(folderModels.get(x).getShareCode().equalsIgnoreCase(shareCode)) {
											modelPath.add(folderFolders.get(k).getfName());
											folderPathString = "";
											for(int s = 0; s<folderPath.size(); s++) {
												folderPathString = folderPathString.concat(folderPath.get(s) + "&");
											}
											folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
											modelPathList.add(folderPathString);

											searchedModels.add(folderModels.get(x));
											System.out.println("model added!2");
											return new ModelAndView("redirect:/api/exportPublicModelService/" + folderModels.get(x).getmName() + "+" + folderPathString);	
										}
									}

								}
								checked.set(y, true);

							}

							checked.add(false);
							listCounter.set(y,j);
							folderPath.add(folderFolders.get(j).getfName());
							modelPath.add(folderFolders.get(j).getfName());
							folderLists.add(folderFolders.get(j).getFolders());
							folderFolders = folderFolders.get(j).getFolders();
							listCounter.add(0);
							y++;
							for(k = 0; k<listCounter.size();k++) {
							}
							j = 0;
						}

						else {
							if(!checked.get(y)) {

								for(k = 0; k<folderFolders.size(); k++) {

									folderModels = folderFolders.get(k).getModels();
									System.out.println(folderFolders.get(k).getfName());
									for(int x = 0; x < folderModels.size(); x++) {
										if(folderModels.get(x).getShareCode().equalsIgnoreCase(shareCode)) {
											folderPathString = "";
											System.out.println("FOLDERPATH " + folderPath);
											modelPath.add(folderFolders.get(k).getfName());
											for(int s = 0; s<folderPath.size(); s++) {
												folderPathString = folderPathString.concat(folderPath.get(s) + "&");
											}

											folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
											modelPathList.add(folderPathString);

											System.out.println("model added!3");
											return new ModelAndView("redirect:/api/exportPublicModelService/" + folderModels.get(x).getmName() + "+" + folderPathString);	
										}
									}
									System.out.println("foldername2: " + folderFolders.get(k).getfName());

								}
								System.out.println(y);
								checked.set(y, true);

							}
							j++;

						}
						for(k = 0; k<checked.size();k++) {
							System.out.println("checkedList: " + checked.get(k));
						}

					}
					System.out.println(searchedFolders.size());

					System.out.println(checkerrorint);
					System.out.println(checkerror);
					System.out.println("fodlertoiterate: " + foldertoiterate);
					System.out.println("prima");
					System.out.println(listCounter);
					System.out.println(folderLists);
					System.out.println(checked);
					System.out.println(folderPath);
					System.out.println(y);

					while(listCounter.get(y) == folderLists.get(y).size() - 1 && y!=0) {

						listCounter.remove(y);
						folderLists.remove(y);
						checked.remove(y);
						folderPath.remove(y-1);
						y--;
					}

					System.out.println("dopo");
					System.out.println(listCounter);
					System.out.println(folderLists);
					System.out.println(checked);
					System.out.println(folderPath);
					System.out.println(y);

					folderFolders = folderLists.get(y);
					for(j = 0; j<folderLists.size();j++) {
					}

					if(y == 0 && (listCounter.get(y) < folderLists.get(y).size() - 1)){

						listCounter.set(y, listCounter.get(y)+1);

						if(listCounter.get(0) != folderLists.get(0).size()) {
							j = listCounter.get(y);
							System.out.println("currentUserFolder: " + folderFolders.get(j).getfName());
							while(folderFolders.get(j).getFolders().isEmpty() && j < folderFolders.size() - 1) {
								j++;
								listCounter.set(y, j);

							}
							if(!folderFolders.get(j).getFolders().isEmpty()) {

								if(!checked.get(y)) {
									for(k = 0; k < folderFolders.size(); k++) {



										folderModels = folderFolders.get(k).getModels();
										for(int x = 0; x < folderModels.size(); x++) {
											if(folderModels.get(x).getShareCode().equalsIgnoreCase(shareCode)) {
												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "&");
												}
												folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
												modelPathList.add(folderPathString);

												searchedModels.add(folderModels.get(x));
												System.out.println("model added!");
												return new ModelAndView("redirect:/api/exportPublicModelService/" + folderModels.get(k).getmName() + "+" + folderPathString);
											}
										}
									}
								}

								folderPath.add(folderFolders.get(j).getfName());
								modelPath.add(folderFolders.get(j).getfName());
								folderLists.add(folderFolders.get(j).getFolders());
								folderFolders = folderFolders.get(j).getFolders();

								System.out.println("not null, got next folder list! 2");
								listCounter.add(0);
								checked.add(false);
							}

							else {
								listCounter.set(y, listCounter.get(y)+1);
							}


							y++;

						}
					}
					else if(y == 0 && (listCounter.get(y) == folderLists.get(y).size() - 1)) {
						y++;
						listCounter.set(0, folderLists.get(0).size()); 
					}


				}
				System.out.println("Folders found are: " + searchedFolders.size());
				System.out.println(folderPathList);
				System.out.println("Models found are: " + searchedModels.size());
				System.out.println(modelPathList);
			}




		}




		return new ModelAndView("redirect:/api/exportModelService/filenotfound");
	}




	@PostMapping("/renamePublicModel/{currentName:.+}+{newName:.+}+{modelPath:.+}")
	public boolean renamePublicModel(@PathVariable String currentName, @PathVariable String newName,
			@PathVariable String modelPath) {

		System.out.println(currentName);
		System.out.println(newName);
		
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = modelPath.split("&");
		boolean found = false;

		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + modelPath + "&" + currentName + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + modelPath + "&" + newName + ".bpmn";
		if (newName.isEmpty() || newName == null)
			return false;

		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {

				if(splittedFolders.length == 1) {
					folderModels = folders.get(i).getModels();

					for (int y = 0; y < folderModels.size(); y++) {
						if (folderModels.get(y).getmName().equalsIgnoreCase(newName)) {
							return false;
						}
					}
					for (int y = 0; y < folderModels.size(); y++) {
						if (folderModels.get(y).getmName().equals(currentName)) {
							folderModels.get(y).setmName(newName);
							folders.get(i).setModels(folderModels);
							publicRepository.save(folders);
							currentPath = currentPath.replace("&", "\\");
							newPath = newPath.replace("&", "\\");
							File model = new File(currentPath);
							model.renameTo(new File(newPath));
							return true;
						}
					}
				}
				folderFolders = folders.get(i).getFolders();
				folderLists.add(folders.get(i).getFolders());
				for (int j = 1; j < splittedFolders.length; j++) {
					found = false;
					for (int k = 0; k < folderFolders.size(); k++) {

						if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
							found = true;
							folderLists.add(folderFolders.get(k).getFolders());
							folderModels = folderFolders.get(k).getModels();
							if(j == splittedFolders.length - 1) {
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equalsIgnoreCase(newName)) {
										return false;
									}
								}
								for (int y = 0; y < folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(currentName)) {
										folderModels.get(y).setmName(newName);
										folderFolders.get(k).setModels(folderModels);
									}
								}

							}
							folderFolders = folderFolders.get(k).getFolders();
						}
					}
				}


				for (int j = folderLists.size() - 1; j <= 0; j--) {
					folderLists.get(j).get(0).setFolders(folderFolders);
				}
				folders.get(i).setFolders(folderLists.get(0));
				publicRepository.save(folders);
			}
		}
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));

		return true;
	}



	@PostMapping("/showPublicModels/{currentFolder}")
	public List<BpmnModel> showPublicModels(@PathVariable String currentFolder) {
		System.out.println("Show Models: " + currentFolder);
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<BpmnModel> modelsFiltered = new ArrayList<BpmnModel>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		String[] splittedFolders = currentFolder.split("&");
		String[] splittedModel;
		String modelName;
		boolean found = false;

		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				if (splittedFolders.length == 1) {
					models = folders.get(i).getModels();
					for(int y = 0; y<models.size(); y++) {
						modelName = models.get(y).getmName();
						splittedModel = modelName.split("-");

						if(splittedModel.length>1 && splittedModel[1].equalsIgnoreCase("version")) {

						}
						else
							modelsFiltered.add(models.get(y));
					}
				} else {
					folderFolders = folders.get(i).getFolders();
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								models = folderFolders.get(k).getModels();
								for(int y = 0; y<models.size(); y++) {
									modelName = models.get(y).getmName();
									splittedModel = modelName.split("-");

									if(splittedModel.length>1 && splittedModel[1].equalsIgnoreCase("version")) {

									}
									else
										modelsFiltered.add(models.get(y));
								}
								folderFolders = folderFolders.get(k).getFolders();
							}
						}
					}
				}
			}
		}
		return modelsFiltered;
	}

	@PostMapping("/movePublicModel/{newPath:.+}+{modelName:.+}+{currentPath:.+}")
	public boolean movePublicModel(@RequestBody User user, @PathVariable String newPath, @PathVariable String modelName,
			@PathVariable String currentPath) {


		BpmnModel modelToMove = new BpmnModel();
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<PublicFolder> folderFolders2 = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<List<PublicFolder>> folderLists2 = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<BpmnModel> folderModels2 = new ArrayList<BpmnModel>();
		String dbCurrentLocation = currentPath;
		String modelNameTxt;
		BigInteger modelId = BigInteger.valueOf(0);
		String[] splittedFolders = dbCurrentLocation.split("&");
		String[] splittedFolders2 = newPath.split("&");
		currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + currentPath;
		// Move the model locally
		boolean success = false;
		boolean found = false;
		if (newPath.equals(""))
			return success;
		modelNameTxt = modelName.concat(".bpmn");
		newPath = newPath.concat("&" + modelNameTxt);
		newPath = newPath.replace("&", "\\");
		currentPath = currentPath.replace("&", "\\").concat("\\" + modelNameTxt);
		File model = new File(currentPath);
		model.renameTo(new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + newPath));
		// move the database reference
		// remove from the current position

		System.out.println("Move Model");
		if (splittedFolders2.length == 1) {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders2[0])) {
					BpmnModel movedModel = new BpmnModel(modelId, modelName);
					List<BpmnModel> models = new ArrayList<BpmnModel>();
					models = folders.get(i).getModels();
					for(int y = 0;y<models.size();y++) {
						if(movedModel.getmName().equalsIgnoreCase(models.get(y).getmName())) {
							return false;
						}
					}
				}
			}
			System.out.println("checkpoint1");
		}

		else {
			System.out.println("checkpoint2");
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders2[0])) {
					folderFolders2 = folders.get(i).getFolders();
					for (int j = 1; j < splittedFolders2.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders2.size(); k++) {
							if (folderFolders2.get(k).getfName().equals(splittedFolders2[j]) && !found) {
								found = true;
								if (j == splittedFolders2.length - 1) {
									System.out.println("checkpoint3");
									BpmnModel movedModel = new BpmnModel(modelId, modelName);
									List<BpmnModel> models = new ArrayList<BpmnModel>();
									models = folderFolders2.get(k).getModels();
									for(int y = 0;y<models.size();y++) {
										if(movedModel.getmName().equalsIgnoreCase(models.get(y).getmName())) {
											return false;
										}
									}
								}
								folderFolders2 = folderFolders2.get(k).getFolders();
								System.out.println("checkpoint4");
							}
						}
					}
				}
			}
		}
		if (splittedFolders.length == 1) {
			System.out.println("checkpoint5");
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders[0])) {
					folderModels = folders.get(i).getModels();
					for (int j = 0; j < folderModels.size(); j++) {
						if (folderModels.get(j).getmName().equals(modelName)) {
							modelToMove = folderModels.get(j);
							modelId = folderModels.get(j).getmId();
							folderModels.remove(j);
							folders.get(i).setModels(folderModels);
							publicRepository.save(folders);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < folders.size(); i++) {
				System.out.println("checkpoint6");
				if (folders.get(i).getfName().equals(splittedFolders[0])) {
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								if (j == splittedFolders.length - 1) {
									folderLists.add(folderFolders.get(k).getFolders());
									folderModels = folderFolders.get(k).getModels();
									System.out.println(folderFolders.get(k).getfName());
									System.out.println(splittedFolders[j]);
									for (int y = 0; y < folderModels.size(); y++) {
										System.out.println("checkpoint7");
										if (folderModels.get(y).getmName().equals(modelName)) {
											modelToMove = folderModels.get(y);
											modelId = folderModels.get(y).getmId();
											folderModels.remove(y);
											folderFolders.get(k).setModels(folderModels);
										}
									}
								}
								folderFolders = folderFolders.get(k).getFolders();
							}
						}
					}
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					folders.get(i).setFolders(folderLists.get(0));
					publicRepository.save(folders.get(i));
					System.out.println("checkpoint8");
				}
			}
		}
		// add to the new position
		if (splittedFolders2.length == 1) {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders2[0])) {
					BpmnModel movedModel = new BpmnModel();
					movedModel = modelToMove;
					List<BpmnModel> models = new ArrayList<BpmnModel>();
					models = folders.get(i).getModels();
					models.add(movedModel);
					folders.get(i).setModels(models);
					publicRepository.save(folders.get(i));
					System.out.println("checkpoint9");
				}
			}
		} else {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders2[0])) {
					folderFolders2 = folders.get(i).getFolders();
					folderLists2.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders2.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders2.size(); k++) {
							if (folderFolders2.get(k).getfName().equals(splittedFolders2[j]) && !found) {
								found = true;
								folderLists2.add(folderFolders2.get(k).getFolders());
								if (j == splittedFolders2.length - 1) {
									System.out.println("checkpoint10");
									BpmnModel movedModel = new BpmnModel();
									movedModel = modelToMove;
									List<BpmnModel> models = new ArrayList<BpmnModel>();
									models = folderFolders2.get(k).getModels();
									models.add(movedModel);
									folderFolders2.get(k).setModels(models);
								}
								folderFolders2 = folderFolders2.get(k).getFolders();
							}
						}
					}
					for (int j = folderLists2.size() - 1; j <= 0; j--) {
						folderLists2.get(j).get(0).setFolders(folderFolders2);
					}
					folders.get(i).setFolders(folderLists2.get(0));
					publicRepository.save(folders.get(i));
					System.out.println("checkpoint11");
				}
			}
		}
		success = true;
		return success;
	}


	@PostMapping("/exportPublicModelCollection/{folderPath:.+}")
	public boolean exportModelCollection(@PathVariable String folderPath, @RequestBody String[] modelList) throws IOException{

		System.out.println(folderPath);
		System.out.println(modelList);
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		List<BpmnModel> modelsToExport = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");
		int modelNumber = 0;
		boolean found = false;


		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				if (splittedFolders.length == 1) {
					models = folders.get(i).getModels();
					for(int y = 0; y<models.size(); y++) {
						for(int z = 0; z<modelList.length; z++) {
							if(models.get(y).getmName().equalsIgnoreCase(modelList[z])) {
								modelsToExport.add(models.get(y));
							}

						}
					}
				} else {
					folderFolders = folders.get(i).getFolders();
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								models = folders.get(i).getModels();
								for(int y = 0; y<models.size(); y++) {
									for(int z = 0; z<modelList.length; z++) {
										if(models.get(y).getmName().equalsIgnoreCase(modelList[z])) {
											modelsToExport.add(models.get(y));
										}

									}
								}
							}
							folderFolders = folderFolders.get(k).getFolders();
						}
					}
				}
			}
		}

		folderPath = folderPath.replace("&", "\\");


		List<File> files = new ArrayList<File>();
		for(int i = 0; i<modelList.length; i++) {
			String path = "C:\\users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + folderPath + "\\" + modelList[i] + ".bpmn";
			File file = new File(path);
			files.add(file);
		}

		File zipfile = new File("C:\\users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\exportedPublicModels.zip");
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


	@GetMapping("/exportPublicModelCollectionService")
	public ResponseEntity<Resource> downloadCollection( HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource
		String filePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\";
		String fileName = "exportedPublicModels.zip";
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


	@PostMapping("/deletePublicCollection")
	public void deletePublicCollection() {

		System.out.println("DELETE COLLECTION");

		File file = new File("C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\exportedPublicModels.zip");
		file.delete();

		return;
	}

	@PostMapping("/showPublicFolders")
	public List<PublicFolder> showPublicFolders() {
		List<PublicFolder> folders = publicRepository.findAll();
		return folders;
	}

	@PostMapping("/showPublicInnerFolders/{currentFolder}")
	public List<PublicFolder> showPublicInnerFolders(@PathVariable String currentFolder) {
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<PublicFolder> foldersToShow = new ArrayList<PublicFolder>();
		String[] splittedFolders = currentFolder.split("&");
		boolean found = false;

		if (splittedFolders.length == 1) {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(currentFolder)) {
					return folders.get(i).getFolders();
				}
			}
		} else {
			for (int j = 0; j < splittedFolders.length; j++) {
				found = false;
				for (int i = 0; i < folders.size(); i++) {
					if (folders.get(i).getfName().equals(splittedFolders[j]) && !found) {
						found = true;
						folders = folders.get(i).getFolders();
						if (j == splittedFolders.length - 1) {
							return folders;
						}
					}
				}
			}
		}
		return null;
	}

	@PostMapping("/checkPublicFolderPath/{currentFolder}")
	public boolean checkPublicFolderPath(@PathVariable String currentFolder) {
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<PublicFolder> foldersToShow = new ArrayList<PublicFolder>();
		String[] splittedFolders = currentFolder.split("&");
		boolean found = false;

		if (splittedFolders.length == 1) {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(currentFolder)) {
					return true;
				}
			}
		} else {
			for (int j = 0; j < splittedFolders.length; j++) {
				found = false;
				for (int i = 0; i < folders.size(); i++) {
					if (folders.get(i).getfName().equals(splittedFolders[j]) && !found) {
						found = true;
						folders = folders.get(i).getFolders();
						if (j == splittedFolders.length - 1) {
							return true;
						}
					}
				}

			}
			return false;
		}
		return false;
	}



	@PostMapping("/addPublicVersionService/{selectedModel:.+}+{versionDescription:.+}+{modelPath:.+}+{username:.+}")
	public boolean addPublicVersionService(@PathVariable String selectedModel, @PathVariable String versionDescription,
			@PathVariable String modelPath, @PathVariable String username) throws IOException {

		System.out.println("description: " + versionDescription);
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<Version> versions = new ArrayList<Version>();
		String[] splittedFolders = modelPath.split("&");
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
		BigInteger modelId = BigInteger.valueOf(0);
		int getPreviousVersion = 0, getModelNumber = 0;
		boolean found = false;
		boolean versionAdded = false;
		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());
		String randomString = gen.nextString();
		String currentDate = getCurrentDate();
		
		System.out.println("checpoint1");
		System.out.println(selectedModel);
		System.out.println(modelPath);
		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				modelId = folders.get(i).getModelsId();
				if (splittedFolders.length == 1) {
					System.out.println("checpoint2");
					folderModels = folders.get(i).getModels();

					for(int y=0; y<folderModels.size(); y++) {
						if (folderModels.get(y).getmName().equals(selectedModel) && !versionAdded) {
							versionAdded = true;
							System.out.println("checkpoint3");
							BpmnModel model = new BpmnModel(folders.get(i).getModelsId().add(BigInteger.ONE), selectedModel);
							getPreviousVersion = folderModels.get(y).getVersionNumber();
							getModelNumber = folderModels.get(y).getModelNumber();
							model.setShareCode(randomString);
							model.setModelNumber(folderModels.get(y).getModelNumber());
							model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
							model.setVersionDescription(versionDescription);
							model.setAuthor(username);
							model.setCreationDate(currentDate);
							folderModels.add(model);
							folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + selectedModel);
							folders.get(i).setModels(folderModels);
							folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
							System.out.println("checkpoint4");
							publicRepository.save(folders);
						}
					}


				} else {
					System.out.println("checpoint5");
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								System.out.println("checpoint6");
								folderModels = folderFolders.get(k).getModels();
								folderLists.add(folderFolders.get(k).getFolders());
								for(int y=0; y<folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(selectedModel) && !versionAdded) {
										versionAdded = true;
										System.out.println("checkpoint7");
										BpmnModel model = new BpmnModel(folders.get(i).getModelsId().add(BigInteger.ONE), selectedModel);
										getPreviousVersion = folderModels.get(y).getVersionNumber();
										getModelNumber = folderModels.get(y).getModelNumber();
										model.setShareCode(randomString);
										model.setModelNumber(folderModels.get(y).getModelNumber());
										model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
										model.setAuthor(username);
										model.setVersionDescription(versionDescription);
										model.setCreationDate(currentDate);
										folderModels.add(model);
										folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + selectedModel);
										folderFolders.get(k).setModels(folderModels);
										System.out.println("checkpoint4");
									}
								}
							}
							folderFolders = folderFolders.get(k).getFolders();
						}
					}
					System.out.println(folderLists);
					System.out.println("checpoint8");
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					System.out.println("checpoint9");
					folders.get(i).setFolders(folderLists.get(0));
					folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
					publicRepository.save(folders);
				}
			}
		}

		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + modelPath + "&" + selectedModel + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + modelPath + "&m" + getModelNumber + "-version-" + getPreviousVersion + "-" + selectedModel + ".bpmn";
		currentPath = currentPath.replace("&", "\\");
		newPath = newPath.replace("&", "\\");
		File model = new File(currentPath);
		model.renameTo(new File(newPath));
		Path source = Paths.get(newPath);
		Path dest = Paths.get(currentPath);
		Files.copy(source, dest, LinkOption.NOFOLLOW_LINKS);

		return true;
	}


	@PostMapping("/getPublicShareCode/{fileName:.+}+{filePath:.+}")
	public ShareCode getShareCode(@PathVariable String fileName, @PathVariable String filePath){
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String dbCurrentLocation = filePath;
		boolean found = false;
		String[] splittedFolders = dbCurrentLocation.split("&");

		ShareCode shareCode = new ShareCode();
		System.out.println(fileName);
		System.out.println(filePath);
		if(splittedFolders.length == 1) {
			for(int i = 0; i<folders.size(); i++) {
				if(folders.get(i).getfName().equalsIgnoreCase(splittedFolders[0])) {
					folderModels = folders.get(i).getModels();
					for (int y = 0; y < folderModels.size(); y++) {
						if (folderModels.get(y).getmName().equals(fileName)) {
							shareCode.setShareCode(folderModels.get(y).getShareCode());
							System.out.println(shareCode);
							return shareCode;
						}
					}
				}
			}
		}

		for(int i = 0; i < folders.size(); i++) {
			if(folders.get(i).getfName().equalsIgnoreCase(splittedFolders[0])) {
				folderFolders = folders.get(i).getFolders();
			}
		}

		for(int i = 1; i<splittedFolders.length; i++) {
			found = false;
			for (int j = 0; j < folderFolders.size(); j++) {
				if (folderFolders.get(j).getfName().equals(splittedFolders[i]) && !found) {
					found = true;
					if(i == splittedFolders.length - 1) {
						folderModels = folderFolders.get(j).getModels();
						for (int y = 0; y < folderModels.size(); y++) {
							if (folderModels.get(y).getmName().equals(fileName)) {
								System.out.println(shareCode);
								shareCode.setShareCode(folderModels.get(y).getShareCode());
							}
						}
					}
					folderFolders = folderFolders.get(j).getFolders();
				}
			}
		}

		return shareCode;
	}



	@PostMapping("/showPublicVersionsService/{selectedModel:.+}+{modelPath:.+}")
	public List<BpmnModel> showPublicVersionsService(@PathVariable String selectedModel, @PathVariable String modelPath) {

		System.out.println(modelPath);
		System.out.println(selectedModel);
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> models = new ArrayList<BpmnModel>();
		List<BpmnModel> modelVersions = new ArrayList<BpmnModel>();
		List<Version> versions = new ArrayList<Version>();
		String[] splittedFolders = modelPath.split("&");
		int modelNumber = 0;
		boolean found = false;

		for(int i=0; i<folders.size(); i++) {
			if(splittedFolders[0].equals(folders.get(i).getfName())) {
				folderFolders = folders.get(i).getFolders();
			}
		}

		if(splittedFolders.length == 1) {
			for(int i=0; i<folders.size(); i++) {

				if(splittedFolders[0].equals(folders.get(i).getfName())) {
					models = folders.get(i).getModels();
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
			}

		}

		else {

			for (int j = 1; j < splittedFolders.length; j++) {
				found = false;
				for (int k = 0; k < folderFolders.size(); k++) {

					if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
						found = true;
						if (j == splittedFolders.length - 1) {
							models = folderFolders.get(k).getModels();
							for (int y = 0; y < models.size(); y++) {
								if (models.get(y).getmName().equals(selectedModel)) {
									modelNumber = models.get(y).getModelNumber();
								}
							}
							for (int y = 0; y < models.size(); y++) {
								if (models.get(y).getModelNumber() == modelNumber) {
									modelVersions.add(models.get(y));
								}
								if(y == models.size()-1) {
									return modelVersions;
								}
							}
						}
						folderFolders = folderFolders.get(k).getFolders();
					}
				}
			}

		}

		return null;
	}



	@PostMapping("/deletePublicModelService/{selectedModel:.+}+{modelPath:.+}")
	public boolean deletePublicModelService(@PathVariable String selectedModel,
			@PathVariable String modelPath) {
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String dbCurrentLocation = modelPath;
		String modelNameTxt;
		String[] splittedFolders = dbCurrentLocation.split("&");
		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + modelPath + "&" + selectedModel + ".bpmn";

		currentPath = currentPath.replace("&", "\\");
		File model = new File(currentPath);
		model.delete();

		if (splittedFolders.length == 1) {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders[0])) {
					folderModels = folders.get(i).getModels();
					for (int j = 0; j < folderModels.size(); j++) {
						if (folderModels.get(j).getmName().equals(selectedModel)) {
							folderModels.remove(j);
							folders.get(i).setModels(folderModels);
							publicRepository.save(folders);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < folders.size(); i++) {
				if (folders.get(i).getfName().equals(splittedFolders[0])) {
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j])) {
								if (j == splittedFolders.length - 1) {
									folderLists.add(folderFolders.get(k).getFolders());
									folderModels = folderFolders.get(k).getModels();
									for (int y = 0; y < folderModels.size(); y++) {
										if (folderModels.get(y).getmName().equals(selectedModel)) {
											folderModels.remove(y);
											folderFolders.get(k).setModels(folderModels);
										}
									}
								}
								folderFolders = folderFolders.get(k).getFolders();
							}
						}
					}
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					folders.get(i).setFolders(folderLists.get(0));
					publicRepository.save(folders.get(i));
				}
			}
		}
		return true;
	}

	@GetMapping("/openPublicModel/{fileName:.+}+{filePath:.+}")
	public ResponseEntity<Resource> openPublicModel(@PathVariable String fileName, @PathVariable String filePath, HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource

		System.out.println("open Model!");


		fileName = fileName.concat(".bpmn");

		Resource resource = storageService.loadPublicFileAsResource(fileName, filePath);

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

	@PostMapping("/deletePublicFolderService/{selectedFolder:.+}+{folderPath:.+}+{deleteType}")
	public boolean deletePublicFolderService(@PathVariable String selectedFolder, @PathVariable String folderPath, @PathVariable String deleteType) throws IOException {

		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		String[] splittedFolders = folderPath.split("&");
		String currentPath = "";
		boolean removed = false; 
		boolean found = false;

		if(folderPath.equals("root"))
			currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + selectedFolder;
		else 	
			currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath + "&" + selectedFolder;
		if(folderPath.equals("root")) {
			currentPath = currentPath.replace("&", "\\");
			FileUtils.deleteDirectory(new File(currentPath));

			for(int i = 0; i<folders.size(); i++) {
				if(folders.get(i).getfName().equalsIgnoreCase(selectedFolder)) {
					publicRepository.delete(folders.get(i));
				}
			}
		}
		else {

			for (int i = 0; i < folders.size(); i++) {

				if (folders.get(i).getfName().equals(splittedFolders[0])) {

					if(deleteType.equals("root") && splittedFolders.length == 1) {
						currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + selectedFolder;
						for(int j = 0; j<folders.size(); j++) {
							if(folders.get(j).getfName().equals(selectedFolder)) {

								publicRepository.delete(folders.get(j));
								currentPath = currentPath.replace("&", "\\");
								FileUtils.deleteDirectory(new File(currentPath));

							}
						}
					}

					else {
						folderFolders = folders.get(i).getFolders();
						folderLists.add(folders.get(i).getFolders());

						if(splittedFolders.length == 1) {
							for(int j = 0; j<folderFolders.size(); j++) {
								if(folderFolders.get(j).getfName().equals(selectedFolder)) {
									folderFolders.remove(j);
									currentPath = currentPath.replace("&", "\\");
									FileUtils.deleteDirectory(new File(currentPath));
									folders.get(i).setFolders(folderFolders);
									publicRepository.save(folders.get(i));
								}
							}
						}

						else {
							for (int j = 1; j < splittedFolders.length; j++) {
								found = false;
								for(int k = 0; k<folderFolders.size(); k++) {
									if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
										found = true;
										if(deleteType.equals("root")) {

											currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath;
											folderLists.add(folderFolders.get(k).getFolders());
											if (j == splittedFolders.length - 1) {
												for(int y = 0; y<folderFolders.size(); y++) {
													if(folderFolders.get(y).getfName().equals(selectedFolder)) {

														folderFolders.remove(y);
														currentPath = currentPath.replace("&", "\\");
														FileUtils.deleteDirectory(new File(currentPath));
														removed = true;
													}
												}

											}
											if(!removed) {
												folderFolders = folderFolders.get(k).getFolders();
											}
										}

										else {
											folderFolders = folderFolders.get(k).getFolders();
											folderLists.add(folderFolders);
											if (j == splittedFolders.length - 1) {
												for(int y = 0; y<folderFolders.size(); y++) {
													if(folderFolders.get(y).getfName().equals(selectedFolder)) {
														folderFolders.remove(y);
														currentPath = currentPath.replace("&", "\\");
														FileUtils.deleteDirectory(new File(currentPath));

													}
												}

											}
										}
									}
								}
							}


							for (int j = folderLists.size() - 1; j <= 0; j--) {
								folderLists.get(j).get(0).setFolders(folderFolders);
							}
							folders.get(i).setFolders(folderLists.get(0));
							publicRepository.save(folders);
						}
					}
				}
			}
		}
		return true;
	}




	@PostMapping("/uploadPublicFileService/{folderPath:.+}+{fileName:.+}+{username:.+}")
	public boolean uploadPublicFileService(@RequestBody User user, @PathVariable String folderPath, @PathVariable String fileName, @PathVariable String username) {


		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");
		String[] splittedFile = fileName.split("\\.");
		BigInteger modelId = BigInteger.valueOf(0);
		boolean found = false;

		fileName = "";
		for(int i = 0; i<splittedFile.length - 1; i++) {
			fileName = fileName.concat(splittedFile[i]);
		}

		for(int i = 0; i<folders.size(); i++) {
			if (modelId.compareTo(folders.get(i).getModelsId()) == -1)
				modelId = folders.get(i).getModelsId();
		}
		System.out.println("modelID: " + modelId);
		String currentDate = getCurrentDate();

		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());

		String randomString = gen.nextString();

		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {

				if (splittedFolders.length == 1) {
					folderModels = folders.get(i).getModels();

					for(int y=0; y<folderModels.size(); y++) {

						if(folderModels.get(y).getmName().equalsIgnoreCase(fileName)) {
							return false;
						}
					}
					System.out.println("checpoint1");
					BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
					model.setShareCode(randomString);
					model.setVersionNumber(1);
					model.setVersionDescription("First version");
					model.setModelNumber(folders.get(i).getModelsId().intValue() + 1);
					model.setCreationDate(currentDate);
					model.setAuthor(username);
					folderModels.add(model);				
					folders.get(i).setModels(folderModels);
					folders.get(i).setModelsId(modelId.add(BigInteger.ONE));
					for(int k = 0; k<folders.size(); k++) {
						folders.get(k).setModelsId(modelId.add(BigInteger.ONE));
					}
					publicRepository.save(folders);
					System.out.println("checpoint2");


					folders = publicRepository.findAll();
					for(int k = 0; k<folders.size(); k++) {
						folders.get(k).setModelsId(modelId.add(BigInteger.ONE));
					}
					return true;
				}

				else {
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								if(j == splittedFolders.length - 1) {
									folderModels = folderFolders.get(k).getModels();
									//folderLists.add(folders.get(k).getFolders());
									for(int y=0; y<folderModels.size(); y++) {

										if(folderModels.get(y).getmName().equalsIgnoreCase(fileName)) {
											return false;
										}
									}
									System.out.println("checpoint3");
									BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
									model.setShareCode(randomString);
									model.setVersionNumber(1);
									model.setVersionDescription("First version");
									model.setModelNumber(folders.get(i).getModelsId().intValue() + 1);
									model.setCreationDate(currentDate);
									model.setAuthor(username);
									folderModels.add(model);	
									folderFolders.get(k).setModels(folderModels);
									System.out.println("checpoint4");
								}
								folderLists.add(folderFolders.get(k).getFolders());
								folderFolders = folderFolders.get(k).getFolders();

							}
						}
					}
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					System.out.println("checpoint5");
					folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
					//folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
					folders.get(i).setFolders(folderLists.get(0));

					publicRepository.save(folders);
				}
			}
		}
		folders = publicRepository.findAll();
		for(int i = 0; i<folders.size(); i++) {
			folders.get(i).setModelsId(modelId.add(BigInteger.ONE));
		}
		publicRepository.save(folders);
		return true;
	}


	@PostMapping("/postPublicFile/{folderPath:.+}+{username:.+}")
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable String folderPath, @PathVariable String username) {
		String message = "";
		folderPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath;
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


	@GetMapping("/exportPublicModelService/{fileName:.+}+{filePath:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, @PathVariable String filePath, HttpServletRequest request) throws MalformedURLException {
		// Load file as Resource
		fileName = fileName.concat(".bpmn");
		Resource resource = storageService.loadPublicFileAsResource(fileName, filePath);

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


	@PostMapping("/publicSearchElements/{searchedElement:.+}+{searchFilter:.+}")
	public PublicSearch publicSearchElements(@PathVariable String searchedElement, @PathVariable String searchFilter) {

		System.out.println("searchedElement: " + searchedElement);
		System.out.println("searchFilter: " + searchFilter);
		//1 name, 2 author, 3 date

		int i,j,k,y = 0;
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<PublicFolder> searchedFolders = new ArrayList<PublicFolder>();
		List<BpmnModel> searchedModels = new ArrayList<BpmnModel>();
		List<Integer> listCounter = new ArrayList<Integer>();
		List<Boolean> checked = new ArrayList<Boolean>();
		PublicSearch search = new PublicSearch();
		List<String> folderPath = new ArrayList<String>();
		List<String> folderPathList = new ArrayList<String>();
		String folderPathString = "";
		String modelPathString = "";
		String[] tempModelName;
		List<String> modelPath = new ArrayList<String>();
		List<String> modelPathList = new ArrayList<String>();
		String foldertoiterate = "";
		folderLists.add(folders);
		String checkerror = "";
		int checkerrorint = 0;
		for(i = 0; i<folders.size(); i++) {
			if(searchFilter.equalsIgnoreCase("1")) {
				if(folders.get(i).getfName().equalsIgnoreCase(searchedElement)) {
					searchedFolders.add(folders.get(i));
					folderPathString = "root";
					folderPathList.add(folderPathString);
					folderPathString = "";
					System.out.println("folder path: " + folderPathList);
					System.out.println("folder added!");


				}
			}
			else if(searchFilter.equalsIgnoreCase("2")) {
				if(folders.get(i).getAuthor().equalsIgnoreCase(searchedElement)) {
					searchedFolders.add(folders.get(i));
					folderPathString = "root";
					folderPathList.add(folderPathString);
					folderPathString = "";
					System.out.println("folder path: " + folderPathList);
					System.out.println("folder added!");


				}
			}
			else if(searchFilter.equalsIgnoreCase("3")) {
				if(folders.get(i).getCreationDate().equalsIgnoreCase(searchedElement)) {
					searchedFolders.add(folders.get(i));
					folderPathString = "root";
					folderPathList.add(folderPathString);
					folderPathString = "";
					System.out.println("folder path: " + folderPathList);
					System.out.println("folder added!");
				}
			}
			folderModels = folders.get(i).getModels();
			for(j = 0; j < folderModels.size(); j++) {
				if(searchFilter.equalsIgnoreCase("1")) {
					if(folderModels.get(j).getmName().equalsIgnoreCase(searchedElement)) {
						folderPathString = "";
						modelPath.add(folders.get(i).getfName());

						folderPathString = folderPathString.concat(folders.get(i).getfName());
						modelPathList.add(folderPathString);
				
						searchedModels.add(folderModels.get(j));
						System.out.println("model added!");
					}
				}
				else if(searchFilter.equalsIgnoreCase("2")) {
					tempModelName = folderModels.get(j).getmName().split("-");
					if(folderModels.get(j).getAuthor().equalsIgnoreCase(searchedElement)) {
						folderPathString = "";
						modelPath.add(folders.get(i).getfName());
				
						if(tempModelName.length > 1) {
							if(!tempModelName[1].equalsIgnoreCase("version")) {
								folderPathString = folderPathString.concat(folders.get(i).getfName());
								modelPathList.add(folderPathString);
								searchedModels.add(folderModels.get(j));
							}
						}
						else {
							folderPathString = folderPathString.concat(folders.get(i).getfName());
							modelPathList.add(folderPathString);
							searchedModels.add(folderModels.get(j));
						}
						System.out.println("model added!");
					}
				}
				else if(searchFilter.equalsIgnoreCase("3")) {
					tempModelName = folderModels.get(j).getmName().split("-");
					System.out.println(folderModels.get(j).getCreationDate());
					if(folderModels.get(j).getCreationDate().equalsIgnoreCase(searchedElement)) {
						folderPathString = "";
						modelPath.add(folders.get(i).getfName());
					
						if(tempModelName.length > 1) {
							if(!tempModelName[1].equalsIgnoreCase("version")) {
								folderPathString = folderPathString.concat(folders.get(i).getfName());
								modelPathList.add(folderPathString);
								searchedModels.add(folderModels.get(j));
							}
						}
						else {
							folderPathString = folderPathString.concat(folders.get(i).getfName());
							modelPathList.add(folderPathString);
							searchedModels.add(folderModels.get(j));
						}
						System.out.println("model added!");
					}
				}
			}

			j = 0;
			while(folders.get(j).getFolders().isEmpty() && j<folders.size()-1) {

				System.out.println("j: " + j);
				j++;
				checkerrorint++;
			}
			checkerror = folders.get(j).getfName() + " is not empty";

			if(!folders.get(j).getFolders().isEmpty()) {
				checked.add(true);
				listCounter.add(j);
				y++;
				modelPath.add(folders.get(j).getfName());
				folderPath.add(folders.get(j).getfName());
				folderLists.add(folders.get(j).getFolders());
				folderFolders = folders.get(j).getFolders();
				foldertoiterate = folders.get(j).getfName();

				System.out.println("not null, got next folder list! 1");
				listCounter.add(0);
				checked.add(false);
			}
			for(j = 0; j<listCounter.size();j++) {
				System.out.println("listcounter: " + listCounter.get(j));
			}


			if(y != 0) {
				while(listCounter.get(y-1) < folderLists.get(y-1).size() - 1 || listCounter.get(0) < folderLists.get(0).size()) {
					System.out.println("start the while in which i loop until the previous folder is not complete");

					if(checked.get(y))
						listCounter.set(y,listCounter.get(y)+1);
					j = listCounter.get(y);
					while(j<folderFolders.size()) {
						if(!folderFolders.get(j).getFolders().isEmpty()) {
							if(!checked.get(y)) {

								for(k = 0; k<folderFolders.size(); k++) {
									folderModels = folderFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(searchFilter.equalsIgnoreCase("1")) {
											tempModelName = folderModels.get(j).getmName().split("-");

											if(folderModels.get(x).getmName().equalsIgnoreCase(searchedElement)) {
												modelPath.add(folderFolders.get(k).getfName());
												folderPathString = "";
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}
												System.out.println("model added!");
											}
										}

										else if(searchFilter.equalsIgnoreCase("2")) {
											tempModelName = folderModels.get(j).getmName().split("-");
											if(folderModels.get(x).getAuthor().equalsIgnoreCase(searchedElement)) {
												modelPath.add(folderFolders.get(k).getfName());
												folderPathString = "";
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

											
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}
												System.out.println("model added!");
											}
										}

										else if(searchFilter.equalsIgnoreCase("3")) {
											tempModelName = folderModels.get(j).getmName().split("-");

											if(folderModels.get(x).getCreationDate().equalsIgnoreCase(searchedElement)) {
												modelPath.add(folderFolders.get(k).getfName());
												folderPathString = "";
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}
												System.out.println("model added!");
											}
										}
									}
									if(searchFilter.equalsIgnoreCase("1")) {
										if(folderFolders.get(k).getfName().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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
									if(searchFilter.equalsIgnoreCase("2")) {
										if(folderFolders.get(k).getAuthor().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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
									if(searchFilter.equalsIgnoreCase("3")) {
										if(folderFolders.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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
							folderPath.add(folderFolders.get(j).getfName());
							modelPath.add(folderFolders.get(j).getfName());
							folderLists.add(folderFolders.get(j).getFolders());
							folderFolders = folderFolders.get(j).getFolders();
							listCounter.add(0);
							y++;
							for(k = 0; k<listCounter.size();k++) {
							}
							j = 0;
						}

						else {
							if(!checked.get(y)) {

								for(k = 0; k<folderFolders.size(); k++) {
									if(searchFilter.equalsIgnoreCase("1")) {
										if(folderFolders.get(k).getfName().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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

									if(searchFilter.equalsIgnoreCase("2")) {
										if(folderFolders.get(k).getAuthor().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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
									if(searchFilter.equalsIgnoreCase("3")) {
										if(folderFolders.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
											searchedFolders.add(folderFolders.get(k));
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


								for(k = 0; k<folderFolders.size(); k++) {

									folderModels = folderFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(searchFilter.equalsIgnoreCase("1")) {
											tempModelName = folderModels.get(j).getmName().split("-");

											if(folderModels.get(x).getmName().equalsIgnoreCase(searchedElement)) {
												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}

												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}

												System.out.println("model added!");
											}
										}
										else if(searchFilter.equalsIgnoreCase("2")) {
											tempModelName = folderModels.get(j).getmName().split("-");

											if(folderModels.get(x).getAuthor().equalsIgnoreCase(searchedElement)) {
												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}

												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}												
												System.out.println("model added!");
											}
										}
										else if(searchFilter.equalsIgnoreCase("3")) {
											tempModelName = folderModels.get(j).getmName().split("-");

											if(folderModels.get(x).getCreationDate().equalsIgnoreCase(searchedElement)) {
												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}

												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}												
												System.out.println("model added!");
											}
										}
									}
								}
								System.out.println(y);
								checked.set(y, true);

							}
							j++;

						}
						for(k = 0; k<checked.size();k++) {
							System.out.println("checkedList: " + checked.get(k));
						}

					}
					System.out.println(searchedFolders.size());

					System.out.println(checkerrorint);
					System.out.println(checkerror);
					System.out.println("fodlertoiterate: " + foldertoiterate);
					System.out.println("prima");
					System.out.println(listCounter);
					System.out.println(folderLists);
					System.out.println(checked);
					System.out.println(folderPath);
					System.out.println(y);

					while(listCounter.get(y) == folderLists.get(y).size() - 1 && y!=0) {

						listCounter.remove(y);
						folderLists.remove(y);
						checked.remove(y);
						folderPath.remove(y-1);
						y--;
					}

					System.out.println("dopo");
					System.out.println(listCounter);
					System.out.println(folderLists);
					System.out.println(checked);
					System.out.println(folderPath);
					System.out.println(y);

					folderFolders = folderLists.get(y);
					for(j = 0; j<folderLists.size();j++) {
					}

					if(y == 0 && (listCounter.get(y) < folderLists.get(y).size() - 1)){

						listCounter.set(y, listCounter.get(y)+1);

						if(listCounter.get(0) != folderLists.get(0).size()) {
							j = listCounter.get(y);
							System.out.println("currentUserFolder: " + folderFolders.get(j).getfName());
							while(folderFolders.get(j).getFolders().isEmpty() && j < folderFolders.size() - 1) {
								j++;
								listCounter.set(y, j);

							}
							if(!folderFolders.get(j).getFolders().isEmpty()) {

								if(!checked.get(y)) {
									for(k = 0; k < folderFolders.size(); k++) {
										if(searchFilter.equalsIgnoreCase("1")) {
											if(folderFolders.get(k).getfName().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(folderFolders.get(k));
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
										if(searchFilter.equalsIgnoreCase("2")) {
											if(folderFolders.get(k).getAuthor().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(folderFolders.get(k));
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
										if(searchFilter.equalsIgnoreCase("3")) {
											if(folderFolders.get(k).getCreationDate().equalsIgnoreCase(searchedElement)) {
												searchedFolders.add(folderFolders.get(k));
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

									folderModels = folderFolders.get(k).getModels();
									for(int x = 0; x < folderModels.size(); x++) {
										if(searchFilter.equalsIgnoreCase("1")) {
											tempModelName = folderModels.get(j).getmName().split("-");
											if(folderModels.get(x).getmName().equalsIgnoreCase(searchedElement)) {

												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												modelPathString = "";
												folderPathString = "";
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}

												System.out.println("model added!");
											}
										}
										if(searchFilter.equalsIgnoreCase("2")) {
											tempModelName = folderModels.get(j).getmName().split("-");
											if(folderModels.get(x).getAuthor().equalsIgnoreCase(searchedElement)) {

												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												modelPathString = "";
												folderPathString = "";
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}												
												System.out.println("model added!");
											}
										}
										if(searchFilter.equalsIgnoreCase("3")) {
											tempModelName = folderModels.get(j).getmName().split("-");
											if(folderModels.get(x).getCreationDate().equalsIgnoreCase(searchedElement)) {

												folderPathString = "";
												modelPath.add(folderFolders.get(k).getfName());
												for(int s = 0; s<folderPath.size(); s++) {
													folderPathString = folderPathString.concat(folderPath.get(s) + "/");
												}

												modelPathString = "";
												folderPathString = "";
												if(tempModelName.length > 1) {
													if(!tempModelName[1].equalsIgnoreCase("version")) {
														folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
														modelPathList.add(folderPathString);
														searchedModels.add(folderModels.get(x));
													}
												}
												else {
													folderPathString = folderPathString.concat(folderFolders.get(k).getfName());
													modelPathList.add(folderPathString);
													searchedModels.add(folderModels.get(x));
												}												
												System.out.println("model added!");
											}
										}
									}
								}

								folderPath.add(folderFolders.get(j).getfName());
								modelPath.add(folderFolders.get(j).getfName());
								folderLists.add(folderFolders.get(j).getFolders());
								folderFolders = folderFolders.get(j).getFolders();

								System.out.println("not null, got next folder list! 2");
								listCounter.add(0);
								checked.add(false);
							}

							else {
								listCounter.set(y, listCounter.get(y)+1);
							}


							y++;

						}
					}
					else if(y == 0 && (listCounter.get(y) == folderLists.get(y).size() - 1)) {
						y++;
						listCounter.set(0, folderLists.get(0).size()); 
					}


				}
				System.out.println("Folders found are: " + searchedFolders.size());
				System.out.println(folderPathList);
				System.out.println("Models found are: " + searchedModels.size());
				System.out.println(modelPathList);
			}




		}

		search.setFolders(searchedFolders);
		search.setModels(searchedModels);
		search.setFolderPaths(folderPathList);
		search.setModelPaths(modelPathList);
		return search;

	}

	@PostMapping("/createPublicModel/{username:.+}+{folderPath:.+}+{fileName:.+}")
	public boolean createPublicModel(@PathVariable String folderPath, @PathVariable String fileName, @PathVariable String username) throws IOException{
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		String[] splittedFolders = folderPath.split("&");
		BigInteger modelId = BigInteger.valueOf(0);
		boolean found = false;
		String source = "C:\\Users\\loren\\git\\SPM-2018-FSB\\emptyModel.bpmn";
		String dest = "C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";

		for(int i = 0; i<folders.size(); i++) {
			if (modelId.compareTo(folders.get(i).getModelsId()) == -1)
				modelId = folders.get(i).getModelsId();
		}
		System.out.println("modelID: " + modelId);
		String currentDate = getCurrentDate();

		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());

		String randomString = gen.nextString();

		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {

				if (splittedFolders.length == 1) {
					folderModels = folders.get(i).getModels();

					for(int y=0; y<folderModels.size(); y++) {

						if(folderModels.get(y).getmName().equalsIgnoreCase(fileName)) {
							return false;
						}
					}
					System.out.println("checpoint1");
					BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
					model.setShareCode(randomString);
					model.setVersionNumber(1);
					model.setVersionDescription("First version");
					model.setModelNumber(folders.get(i).getModelsId().intValue() + 1);
					model.setCreationDate(currentDate);
					model.setAuthor(username);
					folderModels.add(model);				
					folders.get(i).setModels(folderModels);
					folders.get(i).setModelsId(modelId.add(BigInteger.ONE));
					for(int k = 0; k<folders.size(); k++) {
						folders.get(k).setModelsId(modelId.add(BigInteger.ONE));
					}
					publicRepository.save(folders);
					System.out.println("checpoint2");


					folders = publicRepository.findAll();
					for(int k = 0; k<folders.size(); k++) {
						folders.get(k).setModelsId(modelId.add(BigInteger.ONE));
					}
					Path sourcePath = Paths.get(source);
					Path destPath = Paths.get(dest);
					Files.copy(sourcePath, destPath, LinkOption.NOFOLLOW_LINKS);
					return true;
				}

				else {
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								if(j == splittedFolders.length - 1) {
									folderModels = folderFolders.get(k).getModels();
									//folderLists.add(folders.get(k).getFolders());
									for(int y=0; y<folderModels.size(); y++) {

										if(folderModels.get(y).getmName().equalsIgnoreCase(fileName)) {
											return false;
										}
									}
									System.out.println("checpoint3");
									BpmnModel model = new BpmnModel(modelId.add(BigInteger.ONE), fileName);
									model.setShareCode(randomString);
									model.setVersionNumber(1);
									model.setVersionDescription("First version");
									model.setModelNumber(folders.get(i).getModelsId().intValue() + 1);
									model.setCreationDate(currentDate);
									model.setAuthor(username);
									folderModels.add(model);	
									folderFolders.get(k).setModels(folderModels);
									System.out.println("checpoint4");
								}
								folderLists.add(folderFolders.get(k).getFolders());
								folderFolders = folderFolders.get(k).getFolders();

							}
						}
					}
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					System.out.println("checpoint5");
					folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
					//folders.get(i).setModelsId(folders.get(i).getModelsId().add(BigInteger.ONE));
					folders.get(i).setFolders(folderLists.get(0));

					publicRepository.save(folders);
				}
			}
		}
		folders = publicRepository.findAll();
		for(int i = 0; i<folders.size(); i++) {
			folders.get(i).setModelsId(modelId.add(BigInteger.ONE));
		}
		publicRepository.save(folders);
		Path sourcePath = Paths.get(source);
		Path destPath = Paths.get(dest);
		Files.copy(sourcePath, destPath, LinkOption.NOFOLLOW_LINKS);
		return true;

	}


	@PostMapping("/savePublicModel/{folderPath:.+}+{fileName:.+}")
	public void savePublicModel(@PathVariable String folderPath, @PathVariable String fileName, @RequestBody Xmlfile xml) throws IOException{

		Xmlfile xmlToSave = new Xmlfile();
		xmlToSave = xml;
		System.out.println("xml: " + xml.getXml());

		String savePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";

		try (PrintWriter out = new PrintWriter(savePath)) {
			out.println(xml.getXml());
		}

	}
	
	
	@PostMapping("/savePublicModelAsNewVersion/{username:.+}+{folderPath:.+}+{fileName:.+}+{versionDescription:.+}")
	public void savePublicModelAsNewVersion(@PathVariable String username, @PathVariable String folderPath, @PathVariable String fileName, @PathVariable String versionDescription, @RequestBody Xmlfile xml) throws IOException{
		
		System.out.println("description: " + versionDescription);
		List<PublicFolder> folders = publicRepository.findAll();
		List<PublicFolder> folderFolders = new ArrayList<PublicFolder>();
		List<List<PublicFolder>> folderLists = new ArrayList<List<PublicFolder>>();
		List<BpmnModel> folderModels = new ArrayList<BpmnModel>();
		List<Version> versions = new ArrayList<Version>();
		String[] splittedFolders = folderPath.split("&");
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
		BigInteger versionsId = BigInteger.valueOf(0);
		int getPreviousVersion = 0, getModelNumber = 0;
		boolean found = false;
		boolean versionAdded = false;
		RandomGenerator gen = new RandomGenerator(10, ThreadLocalRandom.current());
		String randomString = gen.nextString();
		String currentDate = getCurrentDate();
		
		System.out.println("checpoint1");
		System.out.println(fileName);
		System.out.println(folderPath);
		for (int i = 0; i < folders.size(); i++) {
			if (folders.get(i).getfName().equals(splittedFolders[0])) {
				versionsId = folders.get(i).getVersionsId();
				if (splittedFolders.length == 1) {
					System.out.println("checpoint2");
					folderModels = folders.get(i).getModels();

					for(int y=0; y<folderModels.size(); y++) {
						if (folderModels.get(y).getmName().equals(fileName) && !versionAdded) {
							versionAdded = true;
							System.out.println("checkpoint3");
							BpmnModel model = new BpmnModel(folders.get(i).getModelsId().add(BigInteger.ONE), fileName);
							getPreviousVersion = folderModels.get(y).getVersionNumber();
							getModelNumber = folderModels.get(y).getModelNumber();
							model.setShareCode(randomString);
							model.setModelNumber(folderModels.get(y).getModelNumber());
							model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
							model.setVersionDescription(versionDescription);
							model.setAuthor(username);
							model.setCreationDate(currentDate);
							folderModels.add(model);
							folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + fileName);
							folders.get(i).setModels(folderModels);
							System.out.println("checkpoint4");
							publicRepository.save(folders);
						}
					}


				} else {
					System.out.println("checpoint5");
					folderFolders = folders.get(i).getFolders();
					folderLists.add(folders.get(i).getFolders());
					for (int j = 1; j < splittedFolders.length; j++) {
						found = false;
						for (int k = 0; k < folderFolders.size(); k++) {
							if (folderFolders.get(k).getfName().equals(splittedFolders[j]) && !found) {
								found = true;
								System.out.println("checpoint6");
								folderModels = folderFolders.get(k).getModels();
								folderLists.add(folderFolders.get(k).getFolders());
								for(int y=0; y<folderModels.size(); y++) {
									if (folderModels.get(y).getmName().equals(fileName) && !versionAdded) {
										versionAdded = true;
										System.out.println("checkpoint7");
										BpmnModel model = new BpmnModel(folders.get(i).getModelsId().add(BigInteger.ONE), fileName);
										getPreviousVersion = folderModels.get(y).getVersionNumber();
										getModelNumber = folderModels.get(y).getModelNumber();
										model.setShareCode(randomString);
										model.setModelNumber(folderModels.get(y).getModelNumber());
										model.setVersionNumber(folderModels.get(y).getVersionNumber()+1);
										model.setAuthor(username);
										model.setVersionDescription(versionDescription);
										model.setCreationDate(currentDate);
										folderModels.add(model);
										folderModels.get(y).setmName("m" + folderModels.get(y).getModelNumber() + "-version-" + (folderModels.get(y).getVersionNumber()) + "-" + fileName);
										folderFolders.get(k).setModels(folderModels);
										System.out.println("checkpoint4");
									}
								}
							}
							folderFolders = folderFolders.get(k).getFolders();
						}
					}
					System.out.println(folderLists);
					System.out.println("checpoint8");
					for (int j = folderLists.size() - 1; j <= 0; j--) {
						folderLists.get(j).get(0).setFolders(folderFolders);
					}
					System.out.println("checpoint9");
					folders.get(i).setFolders(folderLists.get(0));
					publicRepository.save(folders);
				}
			}
		}

		String currentPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath + "&" + fileName + ".bpmn";
		String newPath = "C:&Users&loren&git&SPM-2018-FSB&PublicRepository&" + folderPath + "&m" + getModelNumber + "-version-" + getPreviousVersion + "-" + fileName + ".bpmn";
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

		String savePath = "C:\\Users\\loren\\git\\SPM-2018-FSB\\PublicRepository\\" + folderPath.replace("&", "\\") + "\\" + fileName + ".bpmn";

		try (PrintWriter out = new PrintWriter(savePath)) {
			out.println(xml.getXml());
		}

	}
	

	public String getCurrentDate(){
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String currentDate = localDate.format(formatter);
		return currentDate;
	}

	public void addModelToList() {

	}

}


