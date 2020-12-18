<template>
  <img alt="ArqiSoft logo" src="./assets/logo.png" class="logo">
  <Home msg="Checmical Search"/>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import Home from './components/Home.vue';

@Options({
  components: {
    Home,
  },
})
export default class App extends Vue {}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

.logo {
  width: 10rem;
}
</style>
