import { EmployeeDetailedModel } from '@sber-sbertransport/mf-core';

import { IDepartment } from 'stores/Corporate/Corporate.interface';
import { ProfileLinks, ProfileLinksTitles, ProfileLinksType } from './ProfilePage.constants';

export interface IProfileConfig {
  name: ProfileLinksType;
  title?: string;
  description?: string;
  isEditable?: boolean;
  isAvatar?: boolean;
}

export const profileConfig = (self: EmployeeDetailedModel, selfDep: IDepartment | undefined): IProfileConfig[] => [
  {
    name: ProfileLinks.avatar,
    isAvatar: true,
  },
  {
    name: ProfileLinks.fullName,
    description: ProfileLinksTitles.fullName,
    title: self.nameWithInitials,
  },
  {
    name: ProfileLinks.position,
    title: self.position,
    description: ProfileLinksTitles.position,
  },
  {
    name: ProfileLinks.organization,
    title: self.organization,
    description: ProfileLinksTitles.organization,
  },
  {
    name: ProfileLinks.department,
    title: self.department,
    description: ProfileLinksTitles.department,
  },
  {
    name: ProfileLinks.departmentAddress,
    title: selfDep ? selfDep.location : '',
    description: ProfileLinksTitles.departmentAddress,
  },
  {
    name: ProfileLinks.email,
    title: self.email,
    description: ProfileLinksTitles.email,
  },
  {
    name: ProfileLinks.mobilePhone,
    title: self.mobilePhone,
    description: ProfileLinksTitles.mobilePhone,
    isEditable: true,
  },
  {
    name: ProfileLinks.personnellNumber,
    title: self.personnelNumber,
    description: ProfileLinksTitles.personnellNumber,
  },
];
