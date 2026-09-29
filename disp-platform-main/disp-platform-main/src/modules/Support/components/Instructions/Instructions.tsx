import React from 'react';
import { useTranslation } from 'i18n';

import { Tab } from 'components/Tab';
import { TabPane } from 'components/Tab/TabPane';

import InstructionsItems from './components/InstructionsItems/InstructionsItems';
import AllInstructions from './instructions.json';

import styles from './Instructions.module.scss';

const Instructions = () => {
  const t = useTranslation().t.Support.titles;

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>{t.instructions}</h3>

      <Tab>
        {Object.keys(AllInstructions).map(key => (
          <TabPane tab={AllInstructions[key].title} key={key}>
            <InstructionsItems instructions={AllInstructions[key].instructions} />
          </TabPane>
        ))}
      </Tab>
    </div>
  );
};

export default Instructions;
