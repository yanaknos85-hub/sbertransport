import { PersonalOwnerInformation } from './PersonalCars.constants';

export const isOwnerThirdParty = (ownerType?: string): boolean => ownerType === PersonalOwnerInformation.THIRD_PARTY;
