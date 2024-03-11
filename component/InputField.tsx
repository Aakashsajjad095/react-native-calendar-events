import { StyleSheet, Text, TextInput, View } from 'react-native'
import React from 'react'


interface Props{
    onChangeText:(text:string)=>void;
    placeholder:string,
    value:string
}

export const InputField:React.FC<Props> =({
    onChangeText,
    placeholder,
    value
})=>{
  return (
    <View style={styles.container}>
      <TextInput
      style={styles.textInputContainer}
      placeholder={placeholder}
      value={value}
      onChangeText={onChangeText}
      />
      </View>
  )
}



const styles = StyleSheet.create({
    container:{
flex:1
    },
    textInputContainer:{
        borderRadius: 30,
        borderWidth: 1,
        paddingHorizontal: 20,
        paddingVertical: 10,
        fontSize: 18,
        borderColor: '#aaa',
        margin:15
    }
})