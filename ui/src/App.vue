<template>
  <div id="app">
    <a href="http://www.arqisoft.com/" alt="ArqiSoft, LLC" target="_blank" rel="noopener noreferrer">
      <img alt="ArqiSoft logo" src="./assets/images/logo.png" class="logo">
    </a>
    <Search header="Chemical Search"/>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Search from './components/Search.vue';

@Component({
  components: {
    Search,
  },
})
export default class App extends Vue {}
</script>

<style lang="scss">
@import "./assets/styles/styles";

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.logo {
  width: 10rem;
  text-align: center;
}
</style>
