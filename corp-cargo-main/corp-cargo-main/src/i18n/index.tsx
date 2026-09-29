import React, { useContext, useState, useEffect } from 'react';
import { clone } from 'ramda';
import moment from 'moment';
import 'moment/locale/ru';
import { ConfigProvider } from 'antd';

import antdRU from 'antd/lib/locale/ru_RU';
import antdEN from 'antd/lib/locale/en_US';

import { deepMerge, ignore } from 'utils';

import { translation as ru, Translation } from './ru';
import { translation as en } from './en';

// re-exporting stuff
export type { Translation };
export type { NotificationInfo } from './notifications';

const defaultLocale = ru;
const defaultLocaleName = 'ru';

/**
 * Clones default locale and applies translation overrides from a target locale
 * @param locale locale to merge values from to a copy of default locale
 */
const applyLocationOverrides = (locale: Partial<Translation>): Translation => deepMerge(clone(defaultLocale), locale);

const Locales: Record<string, { system: Translation; ant: typeof antdRU }> = {
  ru: { system: ru, ant: antdRU },
  en: { system: applyLocationOverrides(en), ant: antdEN },
};

interface I18nContext {
  setLocale: (locale: keyof typeof Locales) => void;
  t: Translation;
}

const I18nContext = React.createContext<I18nContext>({
  setLocale: ignore,
  t: defaultLocale,
});

/**
 * Decorates component adding I18nContext to react tree.
 * @param WrappedComponent component to decorate
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const internationalized = <P extends Record<string, any>>(
  WrappedComponent: React.ComponentType<P>
): React.ForwardRefExoticComponent<React.PropsWithoutRef<P> & React.RefAttributes<unknown>> => {
  class Wrapper extends React.Component<
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    P & { forwardedRef: React.Ref<any> },
    { t: I18nContext['t']; locale: keyof typeof Locales }
  > {
    state = {
      t: defaultLocale,
      locale: defaultLocaleName,
    };

    setLocale: I18nContext['setLocale'] = locale => {
      this.setState({ t: Locales[locale].system });
      moment().locale(locale);
    };

    render() {
      const { forwardedRef } = this.props;
      const { t, locale } = this.state;
      return (
        <ConfigProvider locale={Locales[locale].ant}>
          <I18nContext.Provider
            value={{
              setLocale: this.setLocale,
              t,
            }}
          >
            <WrappedComponent
              ref={forwardedRef}
              {...this.props}
            />
          </I18nContext.Provider>
        </ConfigProvider>
      );
    }
  }

  return React.forwardRef((props: P, ref) => <Wrapper {...props} forwardedRef={ref} />);
};

export const useHook = () => {
  const [localeState, setLocaleState] = useState({
    t: defaultLocale,
    locale: defaultLocaleName,
  });
  const { t, locale } = localeState;

  const setLocale: I18nContext['setLocale'] = locale => {
    setLocaleState({
      locale,
      t: Locales[locale].system,
    });
    moment().locale(locale);
  };

  useEffect(() => {
    moment.locale(defaultLocaleName);
  }, []);

  return {
    t,
    locale,
    setLocale,
  };
};

export const I18Provider = ({ children }) => {
  const {
    t,
    locale,
    setLocale,
  } = useHook();

  return (
    <ConfigProvider locale={Locales[locale].ant}>
      <I18nContext.Provider
        value={{
          setLocale,
          t,
        }}
      >
        {children}
      </I18nContext.Provider>
    </ConfigProvider>
  );
};

/**
 * hook to be used to fetch translation or to change locale
 */
export const useTranslation = (): I18nContext => {
  const context = useContext(I18nContext);

  if (context === undefined) {
    throw new Error('useTranslation must be used within a I18nContext.Provider');
  }

  return context;
};
