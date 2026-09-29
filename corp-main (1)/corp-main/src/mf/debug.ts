/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-console */
import { devServerProxyTarget } from '@sber-sbertransport/tool-kit/build/mf/constants';
import { getMfShared } from '@sber-sbertransport/tool-kit/build/mf/utils';
import remotes from '../../scripts/remotes';
import pjson from '../../package.json';
import build from '../../version.json';
import mfJson from '../../mf.json';
import * as routes from 'constants/constants.routes';
import * as consts from 'constants/constants.env';
import { isTestMode } from 'utils/isTestMode';

const MF_CONFIG = {
  shared: getMfShared(),
  remotes,
};

const target = devServerProxyTarget[consts.NETWORK_LOOP];

const versions = async () => {
  const err = { ver: 'not loaded' };

  const auth = await (import('auth/version').catch(() => err));
  const platform = await (import('platform/version').catch(() => err));
  const cargo = await (import('cargo/version').catch(() => err));
  const passengers = await (import('passengers/version').catch(() => err));
  const fleet = await (import('fleet/version').catch(() => err));

  console.info(`%c MF versions: `,
    'background: #e800ff; color: #ffffff',
    {
      main: build.ver,
      auth: auth.ver,
      platform: platform.ver,
      cargo: cargo.ver,
      passengers: passengers.ver,
      fleet: fleet.ver,
    }
  );
};

export const debug = () => {
  console.log(`%c MF <${consts.APP_NAME}> debug info: `,
    'background: #222; color: #bada55',
    {
      'APP name': pjson.name,
      'APP version': build.ver,
      'MF json': mfJson,
      'MF name': consts.APP_NAME,
      'MF run as Main': !consts.IS_REMOTE,
      'MF remotes': MF_CONFIG.remotes,
      'App routes': routes,
      'Api proxy': target,
      'TEST MODE': isTestMode(),
      'Auth type': consts.IS_BASIC_AUTH ? 'Log/Pass' : 'Sudir',
      'Dependencies compare (Shared required | In package.json)': {
        'react': `${MF_CONFIG.shared['react']?.requiredVersion} | ${pjson.dependencies['react']}`,
        'react-dom': `${MF_CONFIG.shared['react-dom']?.requiredVersion} | ${pjson.dependencies['react-dom']}`,
        'react-router-dom': `${MF_CONFIG.shared['react-router-dom']?.requiredVersion} | ${pjson.dependencies['react-router-dom']}`,
        '@sber-sbertransport/mf-core': `${MF_CONFIG.shared['@sber-sbertransport/mf-core']?.requiredVersion} | ${pjson.dependencies['@sber-sbertransport/mf-core']}`,
        '@sber-sbertransport/ui-kit': `${MF_CONFIG.shared['@sber-sbertransport/ui-kit']?.requiredVersion} | ${pjson.dependencies['@sber-sbertransport/ui-kit']}`,
        '@sber-sbertransport/tool-kit': `${MF_CONFIG.shared['@sber-sbertransport/tool-kit']?.requiredVersion} | ${pjson.dependencies['@sber-sbertransport/tool-kit']}`,
        'antd': `${MF_CONFIG.shared['antd']?.requiredVersion} | ${pjson.dependencies['antd']}`,
        'styled-components': `${MF_CONFIG.shared['styled-components']?.requiredVersion} | ${pjson.dependencies['styled-components']}`,
      },
    }
  );

  versions();

  return Promise.resolve();
};

export const error = (err: any) => {
  const root = document.getElementById('root');

  if (root) {
    const element = document.createElement('div');

    element.style.textAlign = 'center';
    element.style.fontFamily = 'Arial';
    element.style.fontSize = '20px';
    element.style.margin = '0 auto';
    element.style.width = '500px';

    element.innerHTML = `
      <h4>Внимание!</h4>
      <p>Что-то пошло не так в процессе запуска</p>
      <p>Обратитесь к администратору</p>
    `;

    root.appendChild(element);
  }

  console.info(`%c MF <${consts.APP_NAME}> fatal error! `, 'color: white; background: red; font-size: 14px', err ?? 'неизвестная ошибка');
  console.info('%cВозможное решение:', 'font-weight: bold;', 'Проверьте, что shared версии npm-пакетов во всех установленных микрофронтах одинаковые!');
};

