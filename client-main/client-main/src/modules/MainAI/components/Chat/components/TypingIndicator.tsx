import React from 'react';
import styles from '../Chat.module.scss';

const TypingIndicator = () => (
  <div className={styles.typingIndicator}>
    <span className={styles.typingDot} />
    <span className={styles.typingDot} />
    <span className={styles.typingDot} />
  </div>
);

export default TypingIndicator;
