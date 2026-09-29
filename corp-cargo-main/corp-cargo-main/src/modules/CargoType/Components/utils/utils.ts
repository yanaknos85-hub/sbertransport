import { Roles } from "constants/constants.app";
import { AccessControl, AccessLabeledValue } from "../types/types";

export const getLevels = (role: Roles, currentLevel: AccessControl): AccessControl[] => {
  if (role === Roles.ENGINEER_CORP_CLIENT) {
    if (currentLevel === 'PERSONAL') {
      return ['ORGANIZATION']
    }
    else {
      return [];
    }  
  } else if (role === Roles.ADMIN_DATA_MASTER) {
    if (currentLevel === 'PERSONAL') {
      return ['ORGANIZATION', 'PUBLIC']
    }
    else if (currentLevel === 'ORGANIZATION') {
      return ['PUBLIC']
    }
    else {
      return [];
    }
  }
  return [];
}

export const getLevelOptions = (role: Roles, currentLevel?: AccessControl): AccessLabeledValue[] => {
  const allLabels = {
    PUBLIC: 'Общий',
    PERSONAL: 'Личный',
    ORGANIZATION: 'Организация',
  } as Record<AccessControl, string>;

  let allowedLevels: AccessControl[] = [];
  
  if (role === Roles.ADMIN_DATA_MASTER) {
    allowedLevels = ['ORGANIZATION', 'PUBLIC'];
  } else if (role === Roles.ENGINEER_CORP_CLIENT) {
    allowedLevels = ['ORGANIZATION'];
  }
  
  if (currentLevel && !allowedLevels.includes(currentLevel)) {
    allowedLevels = [currentLevel, ...allowedLevels];
  }
  
  return allowedLevels.map(el => ({
    value: el,
    label: allLabels[el],
  }));
}

export const getMostImportantRole = (userRoles: string[]): Roles | null => {
  const priorityRoles = [
    Roles.ADMIN_DATA_MASTER,
    Roles.ENGINEER_CORP_CLIENT
  ];
  
  return priorityRoles.find(role => userRoles.includes(role)) || null;
};
