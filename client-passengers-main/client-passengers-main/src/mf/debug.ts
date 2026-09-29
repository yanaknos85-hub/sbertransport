/* eslint-disable no-console */
import { devServerProxyTarget } from '@sber-sbertransport/tool-kit/build/mf/constants';
import { getMfShared } from '@sber-sbertransport/tool-kit/build/mf/utils';
import pjson from '../../package.json';
import remotes from '../../scripts/remotes';
import exposes from '../../scripts/exposes';
import * as routes from 'constants/constants.routes';
import * as consts from 'constants/constants.env';

const MF_CONFIG = {
  shared: getMfShared(),
  remotes,
  exposes,
};

const target = devServerProxyTarget[consts.NETWORK_LOOP];

export const debug = () => (
  console.log(`%c MF <${consts.APP_NAME}> debug info: `,
    'background: #222; color: #bada55',
    {
      'APP name': pjson.name,
      'MF name': consts.APP_NAME,
      'MF run as Main': !consts.IS_REMOTE,
      'MF remotes': MF_CONFIG.remotes,
      'MF exposes (Alias : MF src)': MF_CONFIG.exposes,
      'App routes': routes,
      'Api proxy': target,
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
  )
);

export const error = err => {
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

  console.log(`%c MF <${consts.APP_NAME}> fatal error! `, 'color: white; background: red; font-size: 14px', err ?? 'неизвестная ошибка');
  console.log('%cВозможное решение:', 'font-weight: bold;', 'Проверьте, что shared версии npm-пакетов во всех установленных микрофронтах одинаковые!');
};

