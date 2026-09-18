import {RoleTypeEnum} from './enumRoleType';

export interface UpdateUser {
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
  role: RoleTypeEnum;
}
