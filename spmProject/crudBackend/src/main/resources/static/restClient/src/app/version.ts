import { Model } from './model';

export class Version {

    vId: BigInteger;
    vName: String;
    vDescription: String;
    vDate: String;
    models: Model[];
}
