import { StyleSheet, Text, View } from 'react-native'
import React from 'react'


interface Props{
headerTitle:string
}

const Header:React.FC<Props> = ({headerTitle}) => {
  return (
    <View style={styles.container}>
      <Text style={styles.headerTitleStyle}>{headerTitle}</Text>
    </View>
  )
}

export default Header

const styles = StyleSheet.create({
    container:{
        flex:1,
        backgroundColor:'white',
        padding:20,
        justifyContent:'center',
        alignItems:'center'
    },
    headerTitleStyle:{
        fontSize:20
    }
})