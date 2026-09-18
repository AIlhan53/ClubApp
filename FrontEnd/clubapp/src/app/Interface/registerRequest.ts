import {RoleTypeEnum} from './enumRoleType';

export interface registerRequest {
  email: string;
  firstName : string;
  lastName : string;
  password: string;
  role: RoleTypeEnum;
}
