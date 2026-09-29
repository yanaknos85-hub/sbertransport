import React, { useState } from 'react';
import { Input as AntDesignInput, InputRef } from 'antd-mobile';
import cn from 'classnames';

import { IInput } from './IInput';
import { ReactComponent as EyeVisible } from 'assets/icons/eye-visible.svg';
import { ReactComponent as EyeInvisible } from 'assets/icons/eye-invisible.svg';
import styles from './Input.module.scss';

const Input = React.forwardRef<InputRef, IInput>(({
  className, size = 'middle', ...props
}, ref) => {
  const [visible, setVisible] = useState(false);

  const isPassword = props.type === 'password';

  const onVisible = () => {
    setVisible(!visible);
  };

  return (
    <div className={cn(styles.input, 'input-ui', className, {
      [styles.disabled]: props.disabled,
      [styles[size]]: size,
      [styles.password]: isPassword,
    })}
    >
      <AntDesignInput
        {...props}
        ref={ref}
        type={visible ? 'text' : props.type}
      />
      {isPassword && (
        <button
          type="button"
          className={styles.eye}
          onClick={onVisible}
        >
          {visible ? <EyeVisible /> : <EyeInvisible />}
        </button>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
