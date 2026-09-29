import { FleetManagementLinks } from '../Constants';
import { IRouter } from '../Interfaces';

const getFullLinks: IRouter.TGetFullLinks = (appPath: string): string[] => {
  const [, ...links]: string[] = Object.values(FleetManagementLinks) as string[];
  return links.map((link: string): string => `${appPath}/${FleetManagementLinks.fleetManagement}/${link}`);
};

export { getFullLinks };
