import React, { useEffect, useMemo, useRef } from 'react';
import { validateToPattern } from './Pincode.utils';
import { PinInputContainer } from './Pincode.styled';
import PinInputField from './PinInputField';
import { PincodeProps } from './Pincode.types';

const Pincode: React.FC<PincodeProps> = props => {
  const containerRef = useRef(null);

  const completed = useMemo(
    () => props.values.every(val => val),
    [props.values]
  );

  if (completed && props.onComplete) {
    if (props.validate) {
      if (
        props.values.every(val => new RegExp(validateToPattern(props.validate)).test(val)
        )
      ) {
        props.onComplete(props.values);
      }
    } else {
      props.onComplete(props.values);
    }
  }

  useEffect(() => {
    if (completed && containerRef.current) {
      setTimeout(containerRef.current.querySelectorAll('input').forEach(input => input.blur()));
    }
  }, [completed]);

  return (
    <PinInputContainer
      className={props.containerClassName}
      style={props.containerStyle}
      ref={containerRef}
    >
      {props.values.map((value, i) => (
        <PinInputField
          key={props.id ? `${props.id}-${i}` : i}
          index={i}
          value={value}
          completed={completed}
          {...props}
        />
      ))}
    </PinInputContainer>
  );
};

export default Pincode;
