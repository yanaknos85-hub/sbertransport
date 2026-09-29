import { FC } from 'react';

import { TripPurposeIcon } from 'stores/Trip/Trip.interface';

import { ReactComponent as DefaultIcon } from './default.svg';
import { ReactComponent as PURPOSE_CM } from './PURPOSE_CM.svg';
import { ReactComponent as PURPOSE_V_ACCIDENT } from './PURPOSE_V_ACCIDENT.svg';
import { ReactComponent as PURPOSE_V_GOV } from './PURPOSE_V_GOV.svg';
import { ReactComponent as PURPOSE_V_GROUPS } from './PURPOSE_V_GROUPS.svg';
import { ReactComponent as PURPOSE_V_DEBPTORS } from './PURPOSE_V_DEBPTORS.svg';
import { ReactComponent as PURPOSE_GEMBA } from './PURPOSE_GEMBA.svg';
import { ReactComponent as PURPOSE_D_CARD } from './PURPOSE_D_CARD.svg';
import { ReactComponent as PURPOSE_D_VSP } from './PURPOSE_D_VSP.svg';
import { ReactComponent as PURPOSE_DE_NIGHT } from './PURPOSE_DE_NIGHT.svg';
import { ReactComponent as PURPOSE_C_CMP } from './PURPOSE_C_CMP.svg';
import { ReactComponent as PURPOSE_A_PROD } from './PURPOSE_A_PROD.svg';
import { ReactComponent as PURPOSE_MV_VSP } from './PURPOSE_MV_VSP.svg';
import { ReactComponent as PURPOSE_C_CP } from './PURPOSE_C_CP.svg';
import { ReactComponent as PURPOSE_CM_VSP } from './PURPOSE_CM_VSP.svg';
import { ReactComponent as PURPOSE_CH_COLLATERAL } from './PURPOSE_CH_COLLATERAL.svg';
import { ReactComponent as PURPOSE_INVEST } from './PURPOSE_INVEST.svg';
import { ReactComponent as PURPOSE_CM_MMGN } from './PURPOSE_CM_MMGN.svg';
import { ReactComponent as PURPOSE_M_CONTR } from './PURPOSE_M_CONTR.svg';

export const purposeIcons: Record<TripPurposeIcon | 'default', FC> = {
  default: DefaultIcon,
  PURPOSE_CM,
  PURPOSE_V_ACCIDENT,
  PURPOSE_V_GOV,
  PURPOSE_V_GROUPS,
  PURPOSE_V_DEBPTORS,
  PURPOSE_GEMBA,
  PURPOSE_D_CARD,
  PURPOSE_D_VSP,
  PURPOSE_DE_NIGHT,
  PURPOSE_C_CMP,
  PURPOSE_A_PROD,
  PURPOSE_MV_VSP,
  PURPOSE_C_CP,
  PURPOSE_CM_VSP,
  PURPOSE_CH_COLLATERAL,
  PURPOSE_INVEST,
  PURPOSE_CM_MMGN,
  PURPOSE_M_CONTR,
};
