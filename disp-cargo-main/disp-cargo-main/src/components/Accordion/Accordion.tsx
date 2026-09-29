import React, { memo, FC, ReactNode } from 'react';
import { Collapse } from 'antd';
import { CollapseProps } from 'antd/lib/collapse/Collapse';
import styles from './Accordion.module.scss';

const { Panel } = Collapse;

interface DataType {
  description: string | ReactNode;
  header: string;
  key: string;
}

type AccordionProps = CollapseProps & { data: DataType[] };

const Accordion: FC<AccordionProps> = memo(({ data }) => (
  <Collapse
    className={styles.Accordion}
    expandIconPosition="right"
    bordered={false}
  >
    {data.map(panel => (
      <Panel
        key={panel.key}
        header={panel.header}
        className={styles.Panel}
      >
        <span className={styles.Description}>{panel.description}</span>
      </Panel>
    ))}
  </Collapse>
));

export default Accordion;
