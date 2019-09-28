# Required Libraries:
* [Google Cloud Vision](https://cloud.google.com/vision/docs/libraries#client-libraries-install-java)
* [javacpp](https://github.com/bytedeco/javacv/releases)
* [javacpp leptonica](https://github.com/bytedeco/javacv/releases) 
* [javacpp Tesseract](https://github.com/bytedeco/javacv/releases)
* [openCV 4](https://opencv.org/releases/)
* [json-simple](https://github.com/cliftonlabs/json-simple)
* [rest-assured](https://github.com/rest-assured/rest-assured/releases)

## Required Natives:
* javacpp
* javacpp leptonica
* javacpp Tesseract
* openCV 4

**NOTE: Library Natives need to be in the root directory of where the runnable jar is located.**

## Optional Library for Test Cases:
* java-string-similarity

# Execution:
```
java -jar DrugNameOCR.jar <execution type> <args>
```

# Execution Types:
There are three different execution paths, OCR (OCR), Spell Correction Addition (SA) and Candidate Check (CC).

## OCR:
This execution type is for when you want to process an image and extract some drug names with OCR.
	
#### ARGS:
```
"<model dir>" "<HMM path>" "<SpellCOrrectionsMap path>" "<UMLS API key>" "<path to google cloud vision credentials json file>" <img 1 args> <img 2 args>...
```

#### Required Files:
* tesseract data folder "tessdata"
* DrugName.hmm
* Your generated model files in a directory

#### Required Accounts:
* [UMLS](https://www.nlm.nih.gov/research/umls/index.html) - When signing up just say you're on working on a project and they will give you a free login. Took a couple days.
* [Google Cloud Account](https://cloud.google.com/apis/) - They do give a certain amount of units free per month for the google cloud vision API. I think it is around 1000 images a month, after that your account will get charged. You also need to follow some of their documentation on how to enable the permissions for your google cloud account to run the cloud vision api and how to get the json credentials file.
		
##### IMG ARGS:
```
<handler specifier> -I="<img path>"
```
You can have multiple img args to run multiple img processings at once.

##### Handler Specifier:
	-MG is the manual crop specifier to use the google ocr engine
	-AT is the automatic crop specifier to use the tesseract ocr engine
#### Results:
Will be outputted to the same location as the image and the file will be called "<img name>.result"
	
## SA:
This execution type is for when you have ascertained a correct spelling for an original OCR result and want to save it into the spell correction map/dictionary.
		
#### ARGS:
```
"<SpellCorrectionMap path>" <ocr result> <correct spelling>
```
**NOTE: The ocr result and correct spelling arg should only be one word each, it cannot be a sentence.**

## CC:
This execution tpye is for when you want to get some confidence levels for candidate drug names when compared to the original ocr result.
		
#### ARGS:
```
<ocr result> <candidate 1> <candidate 2>...
```
You can have multiple candidate words to check against the original ocr result word.

#### Results:
Will be outputted to the root directory of the program with the file name "<ocr result>.result"
	
