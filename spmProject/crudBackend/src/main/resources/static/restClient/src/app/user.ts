import { FormBuilder } from "@angular/forms";
import { Folder } from "./folder";

export class User {
    id:Number;
    username:string;
    password:string;
    repassword:string;
    name:string;
    surname:string;
    affiliation:string;
    email:string;
    date_of_birth:string;
    folders:Folder[];
    foldersId: BigInteger;
}
